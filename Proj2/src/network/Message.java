package network;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Message<T extends Serializable> implements Serializable {

    static final long serialVersionUID = 42L;

    public enum Type {
        SUCCESSOR,
        PREDECESSOR,
        ITH_FINGER,
        TASK
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

    private Message(Type type, T arg) {
        this.messageType = type;
        this.arg = arg;
    }

    public static <S extends Serializable> Message makeRequest(Type requestType, S arg) {
        Message<S> msg = new Message<>(requestType, arg);
        msg.isRequest = true;
        msg.id = requestCount++;
        return msg;
    }

    public static <S extends Serializable> Message makeResponse(Type requestType, S arg, int id) {
        Message<S> msg = new Message<>(requestType, arg);
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

}
