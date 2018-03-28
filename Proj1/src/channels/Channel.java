package channels;

import service.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class Channel implements Runnable {

    private static final int MAX_MESSAGE_SIZE = 65000;
    private MulticastSocket socket;
    private InetAddress mcastAddr;
    private int mcastPort;
    private Peer parentPeer;
    public Channel(Peer parentPeer, String mcastAddr, String mcastPort) {
        this.parentPeer = parentPeer;

        try {
            this.mcastAddr = InetAddress.getByName(mcastAddr);
            this.mcastPort = Integer.parseInt(mcastPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize();
    }

    private void initialize() {
        try {
            socket = new MulticastSocket(mcastPort);
            socket.setTimeToLive(1);
            socket.joinGroup(mcastAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        byte[] rbuf = new byte[MAX_MESSAGE_SIZE * 2];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        System.out.println("Channel Run!");

        // Loop waiting for messages
        while (true) {

            try { // blocking method
                this.socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("packet length: " + packet.getLength());

            //String msg = new String(packet.getData(), 0, packet.getLength());
            //System.out.println("string length: " + msg.length());
            //System.out.println(msg);

            //this.parentPeer.addMsgToHandler(msg.trim());
            try {
                this.parentPeer.addMsgToHandler(packet.getData(), packet.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        this.close();
    }

    synchronized public void sendMessage(byte[] message) {

        DatagramPacket packet = new DatagramPacket(message, message.length, mcastAddr, mcastPort);

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        socket.close();
    }

    public static enum ChannelType {
        MC, MDB, MDR
    }
}
