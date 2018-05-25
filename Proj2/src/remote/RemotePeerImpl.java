package remote;

import network.Key;
import network.Peer;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.rmi.RemoteException;

public class RemotePeerImpl implements RemotePeer {

    private Peer peer;

    public RemotePeerImpl(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void initiateTask(AdversarialSearchTask task) throws RemoteException {
        peer.initiateTask(task);
    }

    @Override
    public void terminate() throws RemoteException {
        peer.terminate();
    }

    @Override
    public <T extends Serializable> T get(Key key) throws RemoteException {
        return peer.lookup(key);
    }

    @Override
    public void put(Key key, Serializable obj) throws RemoteException {
        peer.put(key, obj);
    }

    @Override
    public String getStatus() throws RemoteException {
        return peer.getStatus();
    }
}
