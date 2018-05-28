package remote;

import network.Key;
import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;
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
    AdversarialSearchTask initiateTask(AdversarialSearchTask task) throws RemoteException;

    /**
     * Gracefully terminate this peer's resources.
     */
    void terminate() throws RemoteException;

    Serializable get(Key key) throws RemoteException;

    void put(Key key, Serializable obj) throws RemoteException;

    InetSocketAddress findSuccessor(Key key) throws RemoteException;

    String getStatus() throws RemoteException;
}
