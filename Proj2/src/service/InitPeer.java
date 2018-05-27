package service;

import network.Peer;
import network.PeerImpl;
import remote.RemotePeer;
import remote.RemotePeerImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class InitPeer {

    /**
     * Default RMI port for setting local interface
     */
    public static final int RMI_PORT = 1099;

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Invalid command line arguments.");
            System.out.println("Usage: <port> [<contact_ip> <contact_port>]");
            System.out.println("If no contact provided, peer will create a new Chord ring.");
            System.exit(1);
        }

        initiateSystemConfigs();

        InetAddress localIP = Utils.getLocalIp();
        InetSocketAddress localAddr = new InetSocketAddress(localIP, Integer.parseInt(args[0]));

        Peer peer = new PeerImpl(localAddr);
        RemotePeer remotePeer = new RemotePeerImpl(peer);
        String remotePeerName = Utils.getNameFromAddress(peer.getAddress());
        bindRemoteObjectStub(remotePeer, remotePeerName);

        if (args.length == 1) {
            peer.create();
        } else {
            InetAddress contactIP = null;
            try {
                contactIP = InetAddress.getByName(args[1]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (contactIP == null) {
                System.err.println("Failed getting contact node InetAddress.");
                System.exit(1);
            }

            InetSocketAddress contactAddr = new InetSocketAddress(contactIP, Integer.parseInt(args[2]));
            peer.join(contactAddr);
        }

        System.out.println("InitPeer: SUCCESS!");
    }

    private static void initiateSystemConfigs(){
        System.setProperty("javax.net.ssl.keyStore", "server.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
    }

    private static void bindRemoteObjectStub(RemotePeer peer, String registryName) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            RemotePeer stub = (RemotePeer) UnicastRemoteObject.exportObject(peer, 0);

            // Get own registry, to rebind to correct stub (port == 1099)
            Registry registry = LocateRegistry.getRegistry();

            // Peer's name in RMI registry is its SocketAddress (ip:port)
            registry.rebind(registryName, stub);

            System.out.println("Remote Interface Ready! Registry name: " + registryName);
        } catch (RemoteException e) {
            System.err.println("Error while binding remote interface to local registry: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
