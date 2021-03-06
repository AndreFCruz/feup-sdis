package network.threads;

import network.ChordNode;
import network.Message;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listens and dispatches incoming requests.
 */
public class Listener extends ThreadImpl {

    private ChordNode node;
    private MessageDispatcher dispatcher;
    private SSLServerSocket serverSocket;
    private ExecutorService executorService;

    public Listener(ChordNode node, MessageDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;

        this.executorService = Executors.newFixedThreadPool(3);

        int port = node.getAddress().getPort();
        this.serverSocket = initServerSocket(port);
    }

    /**
      * Initiates the server socket
      * @param port connection port
      * @return the initialized socket
      */
    public SSLServerSocket initServerSocket(int port) {

        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket;

        try {
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());/**/

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed opening port " + port + ".", e);
        }

        sslServerSocket.setNeedClientAuth(true);

        return sslServerSocket;
    }

    /**
      * Accepts connections and schedules respective handlers
      */
    @Override
    protected void act() {
        final SSLSocket socket;

        try { // block waiting for connections
            socket = (SSLSocket) serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed opening connection.", e);
        }

        executorService.submit(() -> handleConnection(socket));
    }

    /**
      * Frees the socket's resources, terminating
      */
    @Override
    protected void terminate() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
      * Dispatches a connection message
      * @param socket connection socket
      */
    private void handleConnection(Socket socket) {
        ObjectInputStream input;
        ObjectOutputStream output;
        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed creating input/output streams from socket connection.", e);
        }

        Message message = null;
        try {
            message = (Message) input.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed reading object from socket stream.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found locally, check serialVersionUID.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Failed casting read Object to Message.", e);
        }

        Serializable response = null;
        if (message.isRequest()) {
            response = dispatcher.handleRequest(message);
        } else {
            dispatcher.handleResponse(message);
        }

        try {
            // null response indicates an Async request (Task)
            if (response != null)
                output.writeObject(response);
            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed writing object to socket stream.", e);
        }

    }

}
