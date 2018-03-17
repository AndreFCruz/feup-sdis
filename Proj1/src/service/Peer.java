package service;

import java.rmi.RemoteException;

public class Peer implements IService{

    @Override
    public void backup(File file, int replicationDegree) throws RemoteException {
        //start initiators protocols :D
    }

    @Override
    public void restore(File file) throws RemoteException {

    }

    @Override
    public void delete(File file) throws RemoteException {

    }

    @Override
    public void reclaim(int space) throws RemoteException {

    }

    @Override
    public void state() throws RemoteException {

    }
}
