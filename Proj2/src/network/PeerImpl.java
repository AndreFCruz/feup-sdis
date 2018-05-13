package network;

import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class PeerImpl implements Peer {

    private static final int KEY_SIZE = 32;

    private Integer localId;
    private final InetSocketAddress localAddress;
    private InetSocketAddress predecessor;

    private final AtomicReferenceArray<InetSocketAddress> fingers;
    private ConcurrentMap<Integer, Serializable> data;

    // TO DO store successor key on join

    // NOTE add threads for stabilizing, fixing fingers, and listening for requests

    public PeerImpl(InetSocketAddress address) {
        this.localAddress = address;
        this.localId = localAddress.hashCode();

//        this.fingers = new InetSocketAddress[KEY_SIZE];
        this.fingers = new AtomicReferenceArray<>(KEY_SIZE);
        this.data = new ConcurrentHashMap<>();
    }

    @Override
    public int getId() {
        return localId;
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
    public <T extends Serializable> T lookup(int key) {
        // TODO check if key is in this node's range
        return (T) data.get(key);
    }

    @Override
    public <T extends Serializable> T put(int key, Serializable obj) {
        // TODO check if key is in this node's range
        return (T) data.put(key, obj);
    }

    @Override
    public InetSocketAddress getIthFinger(int i) {
        return fingers.get(i);
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
        return false;
    }

    @Override
    public boolean notified(InetSocketAddress predecessor) {
        return false;
    }

    @Override
    public InetSocketAddress findSuccessor(int key) {
        return null;
    }

    @Override
    public void initiateTask(AdversarialSearchTask task) {

    }

    @Override
    public int handleTask(AdversarialSearchTask task) {
        return 0;
    }
}
