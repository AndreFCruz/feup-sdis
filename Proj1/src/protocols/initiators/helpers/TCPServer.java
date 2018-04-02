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
            Log.logError("Couldn't close TCPServer!");
        }
    }

    private void initializeTCPServer() {
        try {
            serverSocket = new ServerSocket(TCPSERVER_PORT + parentPeer.getID());
            Log.logWarning("Started TCPServer!");
            run = true;
        } catch (IOException e) {
            Log.logError("Couldn't initialize TCPServer!");
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
