package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IService extends Remote {

    void backup(byte[] fileData, String pathname, int replicationDegree) throws RemoteException;

    String restore(String pathname) throws RemoteException;

    void delete(String pathname, int type) throws RemoteException;

    void reclaim(int space) throws RemoteException;

    void state() throws RemoteException;

}
