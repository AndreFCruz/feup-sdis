import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Server <port_number> <mcast_addr> <mcast_port>");
            return;
        }

        int serverPort = Integer.parseInt(args[0]);
        int mcastPort = Integer.parseInt(args[2]);
        InetAddress mcastAddr = InetAddress.getByName(args[1]);

        Server server = new Server(serverPort, mcastAddr, mcastPort);
        server.initialize();

        // Send MultiCast Message
        server.broadcastTask.run();

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

}
