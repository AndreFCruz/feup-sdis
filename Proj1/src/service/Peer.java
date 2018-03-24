package service;

import channels.MChannel;
import channels.MDBChannel;
import filesystem.SystemManager;
import network.Message;
import protocols.Handler;
import protocols.initiators.BackupInitiator;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Peer implements IService {

    private MChannel mc;
    private MDBChannel mdb;

    private Handler dispatcher;
    private SystemManager systemManager;

    private int id;
//    private String protocolVersion;
//    private String serverAccessPoint;
//    private IService stub;
//
//    private ArrayList<String> chunkBU = new ArrayList<>();

    public Peer(int id, String[] mcAddress, String[] mdbAddress) throws IOException {
        this.id = id;

        systemManager = new SystemManager(this, 100000);

        mc = new MChannel(this, mcAddress[0], mcAddress[1]);
        mdb = new MDBChannel(this, mdbAddress[0], mdbAddress[1]);

        dispatcher = new Handler(this);

        new Thread(mc).start();
        new Thread(mdb).start();

        new Thread(dispatcher).start();

        System.out.println("Peer " + id + " online!");
    }

    public static void main(String args[]) throws IOException {

//		if (args.length != 2) {
//			System.out.println("Usage: java Peer <mc:port> <mdb:port> <mdl:port>");
//			return;
//		}

        String[] mcAddress = args[1].split(":");
        String[] mdbAddress = args[2].split(":");


        try {
            Peer obj = new Peer(Integer.parseInt(args[0]), mcAddress, mdbAddress);
            IService stub = (IService) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(args[0], stub);
            //registry.bind(args[0], stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }


    }

    public void sendMessage(int channel, Message message) throws IOException {
        switch (channel) {
            case 0:
                mc.sendMessage(message.getBytes());
                break;
            case 1:
                mdb.sendMessage(message.getBytes());
                break;
            default:
                break;
        }
    }


    @Override
    public String backup(File file, int replicationDegree) throws RemoteException {
        new Thread(new BackupInitiator("1.0", file, replicationDegree, this)).start();
        return "backup command ok";
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


    public void addMsgToHandler(String trim) {
        dispatcher.pushMessage(trim);
    }

    public int getID() {
        return id;
    }

    public String getPath(String path) {
        String pathname;

        switch (path){
            case "chunks":
                pathname=systemManager.getChunksPath();
                break;
            case "restores":
                pathname=systemManager.getRestoresPath();
                break;
            default:
                pathname="";
                break;

        }
        return pathname;
    }
}
