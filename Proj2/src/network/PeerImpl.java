package network;

import network.threads.*;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    public PeerImpl(InetSocketAddress address) {
        this.localAddress = address;
        this.localKey = Key.fromAddress(address);

        this.fingers = new AtomicReferenceArray<>(KEY_SIZE);
        this.data = new ConcurrentHashMap<>();

        setUpHelperThreads();
    }

    private void setUpHelperThreads() {
        dispatcher = new MessageDispatcher(this);
        listener = new Listener(this, dispatcher);
        stabilizer = new Stabilizer(this, dispatcher);
        fixFingers = new FixFingers(this);
    }

    private void startHelperThreads() {
        dispatcher.start();
        listener.start();
        stabilizer.start();
        fixFingers.start();
    }

    private boolean isResponsibleForKey(Key key) {
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
    public boolean join(InetSocketAddress contact) {
        if (contact == null || contact.equals(getAddress())) {
            System.err.println("Failed join attempt");
            return false;
        }

        Message<Key> request = Message.makeRequest(Message.Type.SUCCESSOR, getKey());
        InetSocketAddress successorOfKey = dispatcher.requestAddress(contact, request);

        setIthFinger(0, successorOfKey);
        startHelperThreads();

        // NOTE: set contact as predecessor?
        // or await contact's notification?

        return true;
    }

    @Override
    public boolean notify(InetSocketAddress successor) {
        System.out.println("Notifying " + successor + ".");
        if (successor == null || successor.equals(this.getAddress()))
            throw new IllegalArgumentException("Illegal arguments for ChordNode.notify()");

        Message<Serializable> notification = Message.makeRequest(Message.Type.AM_YOUR_PREDECESSOR, getAddress());
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
        return null;
    }

    @Override
    public void initiateTask(AdversarialSearchTask task) {
        // TODO start adversarial search task
        // partition and send to appropriate peers
    }

    @Override
    public int handleTask(AdversarialSearchTask task) {
        // TODO process adversarial search task
        return 0;
    }

    @Override
    public void terminate() {
        stabilizer.terminate();
        fixFingers.terminate();
        listener.toDie();
        // TODO send local data to substitute peer (successor).
    }

    @Override
    public void leave() {}
}
