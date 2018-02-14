import java.io.IOException;
import java.net.*;

public class Server {

    public static void main(String[] args)
            throws IOException {
        if (args.length != 1) {
            System.out.println("Preciso de um porto ooo oooooo");
            return;
        }
        int port = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(port);
        byte[] rbuf = new byte[256];

        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        int i = 0;
        while (i++ < 10) {
            socket.receive(packet);
            String msg = new String(packet.getData()).trim();
            System.out.println("Received: " + msg);
        }

        socket.close();
    }

}