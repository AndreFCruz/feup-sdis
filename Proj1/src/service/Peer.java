package service;

import channels.MChannel;
import channels.MDBChannel;
import protocols.Handler;
import protocols.initiators.BackupInitiator;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Peer implements IService{

    private MChannel mc;
    private MDBChannel mdb;

    private Handler dispatcher;

    private int id;
    private String protocolVersion;
    private String serverAccessPoint;
    private IService stub;

    private ArrayList<String> chunkBU = new ArrayList<>();

    public Peer(){

    }

    public Peer(int id, String[] mcAdress, String[] mdbAdress ) throws IOException {
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;

        mc = new MChannel(mcAdress[0], mcAdress[1]);
        mdb = new MDBChannel(mdbAdress[0], mdbAdress[1]);

//        if (args.length == 0 || args.length == 1) {
//            mcAddress = InetAddress.getByName("224.0.0.0");
//            mcPort = 8000;
//
//            mdbAddress = InetAddress.getByName("224.0.0.0");
//            mdbPort = 8001;
//
//            mdrAddress = InetAddress.getByName("224.0.0.0");
//            mdrPort = 8002;}

        dispatcher = new Handler();

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(dispatcher).start();

  //      if (id == 1)
//            this.stub = (IService) UnicastRemoteObject.exportObject(this, 0);

        System.out.println("All channels online.");
    }

    public static void main(String args[]) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java Peer <mc:port> <mdb:port> <mdl:port>");
            return;
        }

        String[] mcAddress = args[0].split(":");
        String[] mdbAddress = args[1].split(":");


        Peer peer = new Peer(1, mcAddress, mdbAddress);

//        Registry registry;
//        if (peer.id == 1) {
//            String IPV4 = Utils.getIPV4address();
//            System.out.println("My address: " + IPV4);
//            System.setProperty("java.rmi.server.hostname", IPV4);
//            registry = LocateRegistry.createRegistry(Utils.RMI_PORT);
//            registry.bind(peer.serverAccessPoint, peer.getStub());
//        }
//
//        System.out.println("Server is ready.\n\n");



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



    @Override
    public String backup(File file, int replicationDegree) throws RemoteException {
        new Thread(new BackupInitiator(file, replicationDegree)).start();


        return null;
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


}
