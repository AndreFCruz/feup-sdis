import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class Server implements Runnable {

    private static final int TIMEOUT = 10;
    private static final int MAX_MESSAGE_SIZE = 512;

    DatagramSocket socket;
    InetAddress mcastAddr;
    int mcastPort;
    int serverPort;

    TimerTask broadcastTask;
    Timer broadcastTimer;

    Map<String, String> database = new HashMap<>();

    interface RequestHandler {
        String handleRequest(String[] request);
    }

    Map<String, RequestHandler> handlers = new HashMap<>();

    private TimerTask makeMsgTask(String msg) {
        return new TimerTask() {
            @Override
            public void run() {
                byte[] buf = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, mcastAddr, mcastPort);

                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    System.err.println("Failed to broadcast message.");
                }
            }
        };
    }

    private Server(int serverPort, InetAddress mcastAddr, int mcastPort) {
        this.serverPort = serverPort;
        this.mcastAddr = mcastAddr;
        this.mcastPort = mcastPort;

        // TODO populate handlers
    }

    private void initialize() {
        try {
            this.socket = new DatagramSocket(this.serverPort);
            this.socket.setSoTimeout(TIMEOUT * 1000);
        } catch (IOException ex) {
            System.err.println("Failed to create socket.");
            System.exit(1);
        }


        this.broadcastTask = makeMsgTask(Integer.toString(this.serverPort));

        this.broadcastTimer = new Timer("BTimer");
        this.broadcastTimer.scheduleAtFixedRate(this.broadcastTask,1000,1000);

        System.out.println("Server initialized!");
    }

    private void close() {
        socket.close();
        broadcastTimer.cancel();
        broadcastTask.cancel();
        System.out.println("Server terminated!");
    }

    @Override
    public void run() {

        byte[] rbuf = new byte[MAX_MESSAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        // Loop waiting for messages
        while (true) {
            try {
                this.socket.receive(packet);
            } catch (IOException ex) {
                System.out.println("Timeout!");
                break;
            }
            String msg = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + msg.trim() + " from " +
                    packet.getAddress() + ":" + packet.getPort());

            String[] request = msg.split("\\s+");
            String response = new String();
            if (handlers.containsKey(request[0])) {
                response = handlers.get(request[0]).handleRequest(request);
            } else {
                response = "Invalid Request!";
            }

            byte[] sbuf = response.getBytes();
            DatagramPacket responsePkt = new DatagramPacket(sbuf, sbuf.length,
                    packet.getAddress(),
                    packet.getPort());

            try {
                socket.send(responsePkt);
            } catch (IOException ex) {
                System.err.println("Failed to send message.");
            }
        }

        this.close();
    }

}
