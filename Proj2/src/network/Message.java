package network;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Message<T extends Serializable> implements Serializable {

    static final long serialVersionUID = 42L;

    public enum Type {
        SUCCESSOR,      // request node's successor, or key's successor (if provided)
        PREDECESSOR,    // request node's predecessor
        ITH_FINGER,     // request node's ith finger
        TASK,           // request task fulfillment
        KEY,            // request node's key
        GET,            // lookup object
        PUT,            // store object
        AM_YOUR_PREDECESSOR, // indicate this node is target node's predecessor
        OK              // node acknowledges notification
    }

    /**
     * The number of requests sent from this address.
     */
    private static int requestCount = 0;

    /**
     * This message's id.
     * Useful for mapping request messages to response messages.
     * The sender node should keep a table of requestId->requestHandlerTask
     *  to be triggered when response of said request arrives.
     */
    private int id;

    /**
     * This request's type.
     */
    private Type messageType;

    /**
     * This request's argument, if any.
     * May be an integer, or a Task instance.
     */
    private T arg;

    /**
     * Is this a request message (or response) ?
     */
    private boolean isRequest;

    /**
     * The sender's address (ip:port).
     */
    private InetSocketAddress sender;

    private Message(Type type, T arg) {
        this.messageType = type;
        this.arg = arg;
    }

    /**
     * Static method for constructing request messages.
     * @param requestType Type of the message.
     * @param arg The message's argument
     * @param sender The address of the message's sender.
     * @param <S> The type of the message's argument.
     * @return A newly created request message.
     */
    public static <S extends Serializable> Message<S> makeRequest(Type requestType, S arg, InetSocketAddress sender) {
        Message<S> msg = new Message<>(requestType, arg);
        msg.sender = sender;
        msg.isRequest = true;
        msg.id = requestCount++;
        return msg;
    }

    /**
     * Static method for constructing response messages.
     * @param type Type of the message.
     * @param arg The message's argument.
     * @param id The message's id, should be the same as the corresponding request.
     * @param <S> The type of the message's argument.
     * @return A newly created response Message.
     */
    public static <S extends Serializable> Message<S> makeResponse(Type type, S arg, int id) {
        Message<S> msg = new Message<>(type, arg);
        msg.isRequest = false;
        msg.id = id;
        return msg;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return messageType;
    }

    public T getArg() {
        return arg;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public InetSocketAddress getSender() {
        return sender;
    }

}
