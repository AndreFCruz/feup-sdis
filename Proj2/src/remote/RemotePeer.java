package remote;

import network.Key;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePeer extends Remote {

    /**
     * Initiates a Message to the peer-to-peer network.
     * Partitions task in n tasks, and distributes said tasks to
     *  peers in the network, according to the task's hash and
     *  following the Chord implementation.
     * @param task the client's requested task.
     */
    void initiateTask(AdversarialSearchTask task) throws RemoteException;

    /**
     * Gracefully terminate this peer's resources.
     */
    void terminate() throws RemoteException;

    <T extends Serializable> T get(Key key) throws RemoteException;

    void put(Key key, Serializable obj) throws RemoteException;

    String getStatus() throws RemoteException;
}
