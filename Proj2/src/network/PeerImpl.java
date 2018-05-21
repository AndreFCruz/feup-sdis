package network;

import network.threads.*;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;
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

    // NOTE add threads for stabilizing, fixing fingers, and listening for requests

    private MessageDispatcher dispatcher;
    private Listener listener;
    private RecurrentTask stabilizer;
    private RecurrentTask fixFingers;
    private RecurrentTask checkPredecessor;


    public PeerImpl(InetSocketAddress address) {
        this.localAddress = address;
        this.localKey = Key.fromAddress(address);

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
        fixFingers.start();
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
        if (this.isResponsibleForKey(key)) {
            return (T) data.get(key);
        }

        // TODO fetch appropriate successor and send request
        return null;
    }

    @Override
    public void put(Key key, Serializable obj) {
        if (this.isResponsibleForKey(key))
            data.put(key, obj);

        // TODO fetch appropriate successor and send request
    }

    @Override
    public InetSocketAddress getIthFinger(int i) {
        return fingers.get(i);
    }

    @Override
    public void setIthFinger(int i, InetSocketAddress address) {
        if (i == 0) // successor
            this.notify(address);
        fingers.set(i, address);
    }

    @Override
    public void setPredecessor(InetSocketAddress newPredecessor) {
        this.predecessor = newPredecessor;
    }

    @Override
    public void create() {
        startHelperThreads();
        setIthFinger(0, localAddress);
    }

    @Override
    public boolean join(InetSocketAddress contact) {
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
        System.out.println("Notifying " + successor + ".");
        if (successor.equals(this.getAddress())) {
            System.out.println("Notifying self. Successor not set.");
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
        System.out.println("Notified by " + newPred + ".");
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
        InetSocketAddress successor = getSuccessor();
        if (successor == null) {
            return null;
        } else if (isResponsibleForKey(key)) {
            return localAddress;
        }

        InetSocketAddress pred = closestPrecedingNode(key);
        Message<Key> request = Message.makeRequest(Message.Type.SUCCESSOR, key, getAddress());
        return dispatcher.requestAddress(pred, request);
    }


    private InetSocketAddress closestPrecedingNode(Key key) {
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
    public void leave() {
        // TODO check chord protocol's leave
    }
}
