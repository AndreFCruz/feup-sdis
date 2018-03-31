package protocols.initiators.helpers;

import service.Peer;
import utils.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static protocols.ProtocolSettings.TCPSERVER_PORT;

public class TCPServer implements Runnable {
    private ServerSocket serverSocket;
    private Peer parentPeer;
    private boolean run;

    public TCPServer(Peer parentPeer) {
        this.parentPeer = parentPeer;
        initializeTCPServer();
    }

    @Override
    public void run() {
        while (run) {
            handleTCPClient();
        }
    }

    public void closeTCPServer() {
        try {
            run = false;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeTCPServer() {
        try {
            serverSocket = new ServerSocket(TCPSERVER_PORT);
            Log.log("Started TCPServer");
            run = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleTCPClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            Log.log("Received a TCPClient");
            new Thread(new TCPClientHandler(parentPeer, clientSocket)).start();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
