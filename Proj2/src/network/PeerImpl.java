package network;

import network.threads.*;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class PeerImpl implements Peer {

    private static final int KEY_SIZE = 32;

    private Key localKey;
    private final InetSocketAddress localAddress;
    private InetSocketAddress predecessor;

    private final AtomicReferenceArray<InetSocketAddress> fingers;
    private ConcurrentMap<Key, Serializable> data;

    private MessageDispatcher dispatcher;
    private Listener listener;
    private RecurrentTask stabilizer;
    private RecurrentTask fixFingers;
    private RecurrentTask checkPredecessor;


    public PeerImpl(InetSocketAddress address) {
        this.localAddress = address;
        this.localKey = Key.fromAddress(address);
        Logger.log("Starting Peer with Key: " + localKey);

        this.fingers = new AtomicReferenceArray<>(KEY_SIZE);
        this.data = new ConcurrentHashMap<>();

        dispatcher = new MessageDispatcher(this);
        setUpHelperThreads();
    }

    private void setUpHelperThreads() {
        listener = new Listener(this, dispatcher);
        stabilizer = new Stabilizer(this, dispatcher);
        fixFingers = new FixFingers(this);
        checkPredecessor = new CheckPredecessor(this, dispatcher);
    }

    private void startHelperThreads() {
        listener.start();
        stabilizer.start();
//        fixFingers.start(); // TODO uncomment in production
        checkPredecessor.start();
    }

    private boolean isResponsibleForKey(Key key) {
        if (predecessor == null) return false;
        return key.isBetween(Key.fromAddress(predecessor), this.getKey());
    }

    @Override
    public Key getKey() {
        return localKey;
    }

    @Override
    public InetSocketAddress getAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getSuccessor() {
        return fingers.get(0);
    }

    @Override
    public InetSocketAddress getPredecessor() {
        return predecessor;
    }

    @Override
    public <T extends Serializable> T lookup(Key key) {
        InetSocketAddress responsible = findSuccessor(key);
        if (this.localAddress == responsible) {
            return (T) data.get(key);
        }

        Message<Key> request = Message.makeRequest(Message.Type.GET, key, localAddress);
        Message<T> response = dispatcher.sendRequest(responsible, request);
        return response.getArg();
    }

    @Override
    public void put(Key key, Serializable obj) {
        InetSocketAddress responsible = findSuccessor(key);
        if (this.localAddress == responsible) {
            data.put(key, obj);
            return;
        }

        Message<Key> request = Message.makePutRequest(key, obj, localAddress);
        Message response = dispatcher.sendRequest(responsible, request);
        Logger.logWarning("Response to PUT request: " + response.getType());
    }

    @Override
    public InetSocketAddress getIthFinger(int i) {
        return fingers.get(i);
    }

    @Override
    public void setIthFinger(int i, InetSocketAddress address) {
        Logger.log("Setting finger at i=" + i + " -> " + address);
        if (i == 0) // successor
            this.notify(address);
        fingers.set(i, address);
    }

    @Override
    public void setPredecessor(InetSocketAddress newPredecessor) {
        Logger.log("New predecessor. " + predecessor + " -> " + newPredecessor);
        this.predecessor = newPredecessor;
    }

    @Override
    public void create() {
        Logger.log("Creating Chord Ring.");
        startHelperThreads();
        setIthFinger(0, localAddress);
    }

    @Override
    public boolean join(InetSocketAddress contact) {
        Logger.log("Joining Chord at " + contact);
        if (contact == null || contact.equals(getAddress())) {
            System.err.println("Failed join attempt");
            return false;
        }
        startHelperThreads();

        Message<Key> request = Message.makeRequest(Message.Type.SUCCESSOR, getKey(), localAddress);
        InetSocketAddress successorOfKey = dispatcher.requestAddress(contact, request);

        setIthFinger(0, successorOfKey);

        return true;
    }

    @Override
    public boolean notify(InetSocketAddress successor) {
        Logger.log("Notifying " + successor + ".");
        if (successor.equals(this.getAddress())) {
            Logger.log("Notifying self. Successor not set.");
            return false;
        }

        Message<Serializable> notification = Message.makeRequest(
                Message.Type.AM_YOUR_PREDECESSOR,
                getAddress(),
                getAddress());
        Message response = dispatcher.sendRequest(successor, notification);
        return response.getType() == Message.Type.OK;
    }

    @Override
    public void notified(InetSocketAddress newPred) {
        Logger.log("Notified by " + newPred + ".");
        if (newPred == null || newPred.equals(getAddress())) {
            throw new IllegalArgumentException("Illegal arguments for ChordNode.notify()");
        } else if (this.predecessor == null) {
            this.predecessor = newPred;
        }

        Key newPredKey = Key.fromAddress(newPred);
        Key predKey = Key.fromAddress(predecessor);
        if (newPredKey.isBetween(predKey, this.getKey())) {
            this.predecessor = newPred;
        }
    }

    @Override
    public InetSocketAddress findSuccessor(Key key) {
        Logger.log("Finding Successor of key " + key);
        InetSocketAddress successor = getSuccessor();

        if (key.isBetween(this.localKey, Key.fromAddress(successor))) {
            return successor;
        }

        InetSocketAddress pred = closestPrecedingFinger(key);
        if (pred == localAddress)
            return localAddress;

        // TODO change sender address to original request's sender (and make request async) (?)
        Message<Key> request = Message.makeRequest(Message.Type.SUCCESSOR, key, getAddress());
        return dispatcher.requestAddress(pred, request);
    }


    private InetSocketAddress closestPrecedingFinger(Key key) {
        for (int i = fingers.length() - 1; i > 0; i--) {
            InetSocketAddress ithFinger = getIthFinger(i);
            if (ithFinger != null && Key.fromAddress(ithFinger).isBetween(localKey, key))
                return ithFinger;
        }
        return localAddress;
    }

    @Override
    public void initiateTask(AdversarialSearchTask task) {
        // TODO start adversarial search task on separate thread
        // partition and send to appropriate peers
    }

    @Override
    public Future<Integer> handleTask(AdversarialSearchTask task) {
        // TODO process adversarial search task on separate thread
        // returns value of evaluated tree
        return null;
    }

    @Override
    public void terminate() {
        this.leave();
        stabilizer.terminate();
        fixFingers.terminate();
        checkPredecessor.terminate();
        listener.toDie();
    }

    @Override
    public String getStatus() {
        return "Local Address: " + localAddress + "\n" +
                "Key: " + getKey() + "\n" +
                "Successor: " + getSuccessor() + "\n" +
                "Successor's key: " + Key.fromAddress(getSuccessor()) + "\n" +
                "Predecessor: " + getPredecessor() + "\n" +
                "Predecessor's key: " + Key.fromAddress(getPredecessor()) + "\n";
    }

    @Override
    public void leave() {
        // TODO check chord protocol's leave
    }
}
