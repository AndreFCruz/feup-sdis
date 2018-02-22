import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class PlatesServer implements Runnable {

    private static final int TIMEOUT = 10;

    DatagramSocket socket;
    InetAddress mcastAddr;
    int mcastPort;
    int serverPort;

    TimerTask broadcastTask;

    Map<String, String> database;

    private TimerTask makeMsgTask(String msg) {
        return new TimerTask() {
            @Override
            public void run() {
                byte[] buf = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.send(packet);
            }
        };
    }

    private PlatesServer(int serverPort, InetAddress mcastAddr, int mcastPort) {
        this.serverPort = serverPort;
        this.mcastAddr = mcastAddr;
        this.mcastPort = mcastPort;
    }

    private void initialize() {
        this.socket = new DatagramSocket(serverPort);
        this.socket.setSoTimeout(TIMEOUT * 1000);

        this.database = new HashMap<>();
        this.broadcastTask = makeMsgTask(Integer.toString(serverPort));

        System.out.println("Server initialized!");                
    }

    private void close() {
		socket.close();
        System.out.println("Server terminated");
	}

    @Override
    public void run() {
        // Send MultiCast Message
        broadcastTask.run();
        // TODO: send broadcast message in separate method,
        // to be called independently of this thread.

        // Loop waiting for messages
        while (true) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout!");
                break;
            }
            String msg = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + msg.trim() + " from " +
                               packet.getAddress() + ":" + packet.getPort());

            String[] request = msg.split("\\s+");
            String response = new String();

            switch (request[0]){
                case "REGISTER":
                    // to register the association of a plate number to the owners
                    // Returns -1 if the plate number has already been registered;
                    //  otherwise, returns the number of vehicles in the database.

                    if (! isValidPlate(request[1]) || database.containsKey(request[1])) {
                        System.out.println("The plate number has already been registered");
                        response = "-1";
                    } else {
                        database.put(request[1],request[2]);
                        System.out.println("The plate was registed sucessfully");
                        response = Integer.toString(database.size());
                    }
                    break;
                case "LOOKUP":
                    // to get the owner of a given plate number.
                    // Returns the owner's name or the string NOT_FOUND if the plate
                    //  number was never registered.
                    if (! isValidPlate(request[1]) || database.containsKey(request[1])) {
                        System.out.println("The plate number exist in database");
                        response = database.get(request[1]);
                    } else {
                        System.out.println("The plate number doesn't exist in database");
                        response = "NOT_FOUND";
                    }
                    break;
                default:
                    response = "Invalid command";
                    break;
            }
            byte[] sbuf = response.getBytes();
            DatagramPacket responsePkt = new DatagramPacket(sbuf, sbuf.length,
                                                            packet.getAddress(),
                                                            packet.getPort());
            socket.send(responsePkt);

        }

        close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Server <port_number> <mcast_addr> <mcast_port>");
            return;
        }

        int timeout = DEFAULT_TIMEOUT;
        int serverPort = Integer.parseInt(args[0]);
        int mcastPort = Integer.parseInt(args[2]);        
        InetAddress mcastAddr = new InetAddress(args[1]);

        PlatesServer server = new PlatesServer(serverPort, mcastAddr, mcastPort);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
    
    private static boolean isValidPlate(String plate) {
        return plate.matches("\\w{2}-\\w{2}-\\w{2}");
    }
}
