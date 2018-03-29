package service;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBackupService extends Remote {

    String backup(File file, int replicationDegree) throws RemoteException;

    boolean restore(String pathname) throws RemoteException; // TODO return byte[]

    void delete(String pathname) throws RemoteException;

    void reclaim(int space) throws RemoteException;

    void state() throws RemoteException;

}
