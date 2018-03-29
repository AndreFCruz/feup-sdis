package service;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote {

    String backup(File file, int replicationDegree) throws RemoteException;

    String restore(String pathname) throws RemoteException;

    void delete(String pathname) throws RemoteException;

    void reclaim(int space) throws RemoteException;

    void state() throws RemoteException;

}