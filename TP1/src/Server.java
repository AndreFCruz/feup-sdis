import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static void main(String[] args) throws IOException {
        if (args.length != 1 && args.length != 2) {
            System.out.println("Usage: java Server <port_number> [<timeout>]");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);
        DatagramSocket socket = new DatagramSocket(port);
        socket.setSoTimeout(timeout * 1000);

        byte[] rbuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        System.out.println("Server initialized!");

        Map<String , String> database = new HashMap<>(); //plate number -> owner name

        int i = 0;
        while (i++ < 20) {
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
        socket.close();
        System.out.println("Server terminated");
    }
    
    private static boolean isValidPlate(String plate) {
        return plate.matches("\\w{2}-\\w{2}-\\w{2}");
    }
}
