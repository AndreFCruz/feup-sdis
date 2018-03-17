package service;

import java.rmi.Remote;

public interface IService extends Remote {

    void backup(File file, int replicationDegree) throws RemoteException;

    void restore(File file) throws RemoteException;

    void delete(File file) throws RemoteException;

    void reclaim(int space) throws RemoteException;

    void state() throws RemoteException;

}
