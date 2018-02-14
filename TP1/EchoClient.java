import java.io.IOException;
import java.net.*;

public class EchoClient {

	public static void main(String[] args) throws IOException{
		if (args.length != 3) {
			System.out.println("Usage: java Echo <hostname> <port> <string to echo>");
			return;
		}
		int port = Integer.parseInt(args[1]);
		
		// send request	
		DatagramSocket socket = new DatagramSocket();
		byte[] sbuf = args[2].getBytes();						// string to echo
		InetAddress address	= InetAddress.getByName(args[0]);	// hostname
		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);
		socket.send(packet);
		System.out.println(packet.getData().toString());

		// get response
		byte[] rbuf	= new byte[sbuf.length];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		// display response
		String received	= new String(packet.getData());
		System.out.println("Echoed	Message:" + received);
		socket.close();
	}
}