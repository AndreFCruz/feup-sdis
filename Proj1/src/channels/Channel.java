package channels;

import service.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class Channel implements Runnable {

    public enum ChannelType {
        MC, MDB, MDR
    }

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

        byte[] rbuf = new byte[MAX_MESSAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        // Loop waiting for messages
        while (true) {

            try { // blocking method
                this.socket.receive(packet);
                this.parentPeer.addMsgToHandler(packet.getData(), packet.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    synchronized public void sendMessage(byte[] message) throws IOException {

        DatagramPacket packet = new DatagramPacket(message, message.length, mcastAddr, mcastPort);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }

}
