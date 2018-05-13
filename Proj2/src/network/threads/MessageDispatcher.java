package network.threads;

import network.Message;
import network.Peer;

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

    private Peer peer;
    private ExecutorService executorService;
    private ConcurrentMap<Integer, ResponseHandler> responseHandlers;

    public MessageDispatcher(Peer peer) {
        this.peer = peer;
        this.executorService = Executors.newFixedThreadPool(5);
        this.responseHandlers = new ConcurrentHashMap<>();
    }

    public int requestKey(InetSocketAddress server) {
        Message request = Message.makeRequest(Message.Type.KEY, null);
        Message response = sendRequest(server, request);

        return (int) response.getArg();
    }

    public InetSocketAddress requestAddress(InetSocketAddress server, Message request) {
        if (request.getType() == Message.Type.TASK)
            throw new IllegalArgumentException("requestAddress called with Task message.");

        Message response = sendRequest(server, request);
        if (request.getType() != response.getType() || request.getId() != response.getId()) {
            System.err.println("Response incompatible with request message.");
            return null;
        }

        InetSocketAddress address = null;
        try {
            address = (InetSocketAddress) response.getArg();
        } catch (ClassCastException e) {
            System.err.println("Failed casting response arg to InetSocketAddress. " + e.getMessage());
        }

        return address;
    }

    public <S1 extends Serializable> Message sendRequest(InetSocketAddress server, Message<S1> msg) {
        if (server == null || msg == null || ! msg.isRequest())
            throw new IllegalArgumentException("Invalid message to be sent as request.");

        Socket socket = sendMessage(server, msg);

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
        Socket socket = sendMessage(server, msg);
        return responseHandlers.put(msg.getId(), callback);
    }

    public void sendResponse(InetSocketAddress server, Message msg) {
        Socket socket = sendMessage(server, msg);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket sendMessage(InetSocketAddress server, Message msg) {
        if (server == null || msg == null)
            throw new IllegalArgumentException("sendMessage received NULL arguments.");

        Socket socket = null;
        try {
            socket = new Socket(server.getAddress(), server.getPort());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(msg);
        } catch (IOException e) {
            throw new RuntimeException("Failed connecting to server socket.", e);
        }

        return socket;
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

    public Message handleRequest(Message request) {
        // TODO switch among request types

        switch (request.getType()) {
            case SUCCESSOR:
                break;
            case PREDECESSOR:
                break;
            case ITH_FINGER:
                break;
            case TASK:

                break;
            default:
                System.err.println("Invalid message type received.");
        }

        return null;
    }

}
