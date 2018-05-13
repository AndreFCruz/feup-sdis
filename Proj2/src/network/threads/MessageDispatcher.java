package network.threads;

import network.ChordNode;
import network.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Message Dispatcher class.
 * Handles incoming and outgoing requests, as well as incoming and outgoing responses.
 */
public class MessageDispatcher extends Thread {
    public interface ResponseHandler {
        void handleResponse(Message msg);
    }

    /**
     * Response wait time, in milliseconds.
     */
    static final int RESPONSE_WAIT_TIME = 100;

    private ChordNode node;
    private ExecutorService executorService;
    private ConcurrentMap<Integer, ResponseHandler> responseHandlers;

    public MessageDispatcher(ChordNode node) {
        this.node = node;
        this.executorService = Executors.newFixedThreadPool(5);
        this.responseHandlers = new ConcurrentHashMap<>();
    }

    public <S extends Serializable> Message sendRequest(InetSocketAddress server, Message<S> msg) {
        if (server == null || msg == null || ! msg.isRequest())
            throw new IllegalArgumentException("Invalid message to be sent as request.");

        Socket socket = null;
        try {
            socket = new Socket(server.getAddress(), server.getPort());
            sendMessage(socket, msg);
        } catch (IOException e) {
            throw new RuntimeException("Failed connecting to server socket.", e);
        }

        try {
            Thread.sleep(RESPONSE_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message response = null;
        try {
            response = getResponse(socket);
        } catch (IOException e) {
            throw new RuntimeException("Failed connecting to input socket.", e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed closing socket.", e);
        }

        return response;
    }

    public <S extends Serializable> ResponseHandler sendRequestAsync(final InetSocketAddress server,
                                                            final Message<S> msg, ResponseHandler callback) {
        try {
            Socket socket = new Socket(server.getAddress(), server.getPort());
            sendMessage(socket, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseHandlers.put(msg.getId(), callback);
    }

    private <S extends Serializable> void sendMessage(Socket socket, Message<S> msg) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(msg);
    }

    // Add <S extends Serializable> bound to return ?
    private Message getResponse(Socket socket) throws IOException {
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        try {
            return (Message) input.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFound, check serialization settings.");
        } catch (ClassCastException e) {
            System.err.println("ClassCastException. Object received must be of type Message.");
        }

        return null;
    }

    public <S extends Serializable> void handleResponse(final Message<S> response) {
        if (response == null || response.isRequest())
            throw new IllegalArgumentException("Illegal messaged passed to handleResponse.");

        ResponseHandler handler = responseHandlers.get(response.getId());
        if (handler == null) {
            System.err.println("Received response with unregistered handler.");
        } else {
            executorService.submit(() -> handler.handleResponse(response));
        }
    }

    public <S extends Serializable> void handleRequest(Message<S> request) {
        // TODO switch among request types
    }

}
