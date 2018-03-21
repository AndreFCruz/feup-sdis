package service;

import filesystem.File;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements IService{



    public Peer() {
    }

    @Override
    public void backup(byte[] fileData, String pathname, int replicationDegree) throws RemoteException {
        //start initiators protocols :D
    }

    @Override
    public String restore(String pathname) throws RemoteException {
    	System.out.println("ola");
    	return "ola";
    }

    @Override
    public void delete(String pathname, int type) throws RemoteException {

    }

    @Override
    public void reclaim(int space) throws RemoteException {

    }

    @Override
    public void state() throws RemoteException {

    }

    public static void main(String args[]) {

        try {
            Peer obj = new Peer();
            IService stub = (IService) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Hello", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
