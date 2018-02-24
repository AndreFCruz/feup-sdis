import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Client {

	private static final int MAX_MESSAGE_LEN = 512;

	public static void main(String[] args) throws IOException {
		if (args.length < 4) {
			System.out.println("Usage: java Client <mcast_addr> <mcast_port> <oper> <opnd>*");
			return;
		}

		InetAddress mcastAddr = InetAddress.getByName(args[0]);	// hostname
		int mcastPort = Integer.parseInt(args[1]); //port
		String request = args[2]; //register or lookup
		String plate = args[3]; //plate number

		MulticastSocket mcastSocket = new MulticastSocket(mcastPort);
		mcastSocket.joinGroup(mcastAddr);

		byte[] buf = new byte[MAX_MESSAGE_LEN];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		mcastSocket.receive(packet);
		mcastSocket.leaveGroup(mcastAddr);
		mcastSocket.close();

		String msg = new String(buf, 0, buf.length);
//		System.out.println(msg);

		InetAddress serverAddr = packet.getAddress();

		if (! msg.trim().matches("\\d{1,4}")) {
			throw new RuntimeException("Invalid PORT received: " + msg);
		}

		int serverPort = Integer.parseInt(msg.trim());

		if(!plate.matches("\\w{2}-\\w{2}-\\w{2}")) {
			System.out.println("The plate format is incorrect. Please insert with format XX-XX-XX");
			return;
		}

		String message = new String();

		switch (request.toLowerCase()) {
			case "register":
				String operand = args[4];
				if(operand.length() > MAX_MESSAGE_LEN) {
					System.out.println("The vehicle owner's name must have less than "
							+ MAX_MESSAGE_LEN + " characters.");
					return;
				}
				message = "REGISTER " + plate + " " + operand;
				break;
			case "lookup":
				message = "LOOKUP " + plate;
				break;
			default:
				System.out.print("Invalid Client Request.");
				return;
		}

		DatagramSocket socket = sendMessage(message, serverAddr, serverPort);
		String response = receiveMessage(socket, serverAddr, serverPort);

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
		byte[] rbuf = new byte[MAX_MESSAGE_LEN];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		// display response
		String received = new String(packet.getData());

		return received;
	}

}
