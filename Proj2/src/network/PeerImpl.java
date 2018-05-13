package network;

import network.threads.Listener;
import network.threads.MessageDispatcher;
import network.threads.Stabilizer;
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
    private Stabilizer stabilizer;

    public PeerImpl(InetSocketAddress address) {
        this.localAddress = address;
        this.localKey = Key.fromAddress(address);

        this.fingers = new AtomicReferenceArray<>(KEY_SIZE);
        this.data = new ConcurrentHashMap<>();
    }

    private void startHelperThreads() {
        dispatcher = new MessageDispatcher(this);
        dispatcher.start();

        listener = new Listener(this, dispatcher);
        listener.start();

        stabilizer = new Stabilizer(this, dispatcher);
        stabilizer.start();
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
    public <T extends Serializable> T put(Key key, Serializable obj) {
        if (this.isResponsibleForKey(key)) {
            return (T) data.put(key, obj);
        }

        // TODO fetch appropriate successor and send request
        return null;
    }

    @Override
    public InetSocketAddress getIthFinger(int i) {
        return fingers.get(i);
    }

    @Override
    public void setIthFinger(int i, InetSocketAddress address) {
        fingers.set(i, address);
    }

    @Override
    public boolean join(InetSocketAddress contact) {
        return false;
    }

    @Override
    public void leave() {

    }

    @Override
    public boolean notify(InetSocketAddress successor) {
        if (successor == null || successor.equals(this.getAddress()))
            return false;

        Message<Serializable> notification = Message.makeRequest(Message.Type.AM_YOUR_PREDECESSOR, getAddress());
        Message response = dispatcher.sendRequest(successor, notification);
        return response.getType() == Message.Type.OK;
    }

    @Override
    public void notified(InetSocketAddress newPred) {
        if (newPred == null || newPred.equals(getAddress())) {
            return;
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

    }

    @Override
    public int handleTask(AdversarialSearchTask task) {
        return 0;
    }

    @Override
    public void terminate() {
        stabilizer.terminate();
        listener.toDie();
        // TODO send local data to substitute peer (successor).
    }
}
