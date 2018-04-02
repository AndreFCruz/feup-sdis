package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBackupService extends Remote {

    void backup(String pathname, int replicationDegree) throws RemoteException;

    void restore(String pathname) throws RemoteException;

    void delete(String pathname) throws RemoteException;

    void reclaim(int space) throws RemoteException;

    void state() throws RemoteException;

}
