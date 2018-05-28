package remote;

import network.Key;
import network.Peer;
import task.AdversarialSearchTask;
import task.GameState;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;

public class RemotePeerImpl implements RemotePeer {

    private Peer peer;

    public RemotePeerImpl(Peer peer) {
        this.peer = peer;
    }

    @Override
    public GameState initiateTask(AdversarialSearchTask task) throws RemoteException {
        return peer.initiateTask(task);
    }

    @Override
    public void terminate() throws RemoteException {
        peer.terminate();
    }

    @Override
    public Serializable get(Key key) throws RemoteException {
        return peer.lookup(key);
    }

    @Override
    public void put(Key key, Serializable obj) throws RemoteException {
        peer.put(key, obj);
    }

    @Override
    public InetSocketAddress findSuccessor(Key key) throws RemoteException {
        return peer.findSuccessor(key);
    }

    @Override
    public String getStatus() throws RemoteException {
        return peer.getStatus();
    }
}
