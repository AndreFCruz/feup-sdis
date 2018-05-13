package network.threads;

import network.ChordNode;
import network.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
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
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public Listener(ChordNode node, MessageDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;

        this.executorService = Executors.newFixedThreadPool(3);

        int port = node.getAddress().getPort();
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed opening port " + port + ".", e);
        }
    }

    @Override
    protected void act() {
        final Socket socket;

        try { // block waiting for connections
            socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed opening connection.", e);
        }

        executorService.submit(() -> handleConnection(socket));
    }

    @Override
    protected void terminate() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        ObjectInputStream input;
        ObjectOutputStream output;
        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed creating input/output streams from socket connection.", e);
        }

        Message request = null;
        try {
            request = (Message) input.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed reading object from socket stream.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found locally, check serialVersionUID.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Failed casting read Object to Message.", e);
        }

        Serializable response = dispatcher.handleRequest(request);
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
