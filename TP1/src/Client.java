import java.io.IOException;
import java.net.*;

public class Client {
    
    private static final int MESSAGE_LEN = 256;

	public static void main(String[] args) throws IOException {
		if (args.length < 4) {
			System.out.println("Usage: java Client <hostname> <port> <oper> <opnd>*");
			return;
		}

		InetAddress address	= InetAddress.getByName(args[0]);	// hostname
		int port = Integer.parseInt(args[1]); //port
		String request = args[2]; //register or lookup
		String plate = args[3]; //plate number

		if(!plate.matches("\\w{2}-\\w{2}-\\w{2}")) {
			System.out.println("The plate format is incorrect. Please insert with format XX-XX-XX");
			return;
		}

		String message = new String();

		switch (request) {
			case "register":
				if(args[4].length() > 256) {
					System.out.println("The vehicle owner's name must have less than 256 characters.");
					return;
				}
				message = "REGISTER " + args[3] + " " + args[4];
				break;
			case "lookup":
				message = "LOOKUP " + args[3];
				break;
			default:
				System.out.print("Invalid Client Request.");
				return;
		}

        DatagramSocket socket = sendMessage(message, address, port);
        String response = receiveMessage(socket, address, port);

		System.out.println("Echoed Message:" + response);
		socket.close();

		System.out.println("Client terminated!");
	}
    
    private static DatagramSocket sendMessage(String message, InetAddress address, int port) throws IOException {
        System.out.println(message);
        DatagramSocket socket = new DatagramSocket();
        byte[] sbuf = message.getBytes(); // send buffer
        
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
        socket.send(packet);
        
        return socket;
    }
    
    private static String receiveMessage(DatagramSocket socket, InetAddress address, int port) throws IOException {
        // get response
        byte[] rbuf = new byte[MESSAGE_LEN];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        // display response
        String received = new String(packet.getData());
        
        return received;
    }
    
}
