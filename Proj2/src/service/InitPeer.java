package service;

import network.Peer;
import network.PeerImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class InitPeer {
    public static void main(String[] args) {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Invalid command line arguments.");
            System.out.println("Usage: <port> [<contact_ip> <contact_port>]");
            System.out.println("If no contact provided, peer will create a new Chord ring.");
            System.exit(1);
        }

        InetAddress localIP = null;
        try {
            localIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        InetSocketAddress localAddr = new InetSocketAddress(localIP, Integer.parseInt(args[0]));

        Peer peer = new PeerImpl(localAddr);
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

        System.out.println("SUCCESS");
    }
}
