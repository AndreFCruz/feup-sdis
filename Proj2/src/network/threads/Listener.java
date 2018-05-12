package network.threads;

import network.ChordNode;
import network.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Listens and dispatches incoming requests.
 */
public class Listener extends ThreadImpl {

    private ChordNode node;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public Listener(ChordNode node) {
        this.node = node;
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
    protected void initialize() { }

    @Override
    protected void act() {
        final Socket socket;

        try {
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

        Request request = null;
        try {
            request = (Request) input.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed reading object from socket stream.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found locally, check serialVersionUID.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Failed casting read Object to Request.", e);
        }

        Object response = handleRequest(request);
        try {
            output.writeObject(response);
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed writing object to socket stream.", e);
        }

    }

    private Object handleRequest(Request request) {
        // TODO
        System.out.println(request.getType() + " - " + request.getArg());
        return null;
    }
}
