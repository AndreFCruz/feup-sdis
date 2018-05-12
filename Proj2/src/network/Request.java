package network;

import task.AdversarialSearchTask;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Request implements Serializable {

    static final long serialVersionUID = 42L;

    public enum Type {
        SUCCESSOR_OF,
        PREDECESSOR_OF,
        ITH_FINGER,
        TASK
    }

    /**
     * The number of requests sent from this address.
     */
    private static int requestCount = 0;

    /**
     * This request's id.
     * Useful for parallelizing answers to various requests.
     * The sender node should keep a table of requestId->requestHandlerTask
     *  to be triggered when response of said request arrives.
     */
    private int id = requestCount++;

    /**
     * The address if this request's sender.
     */
    private InetSocketAddress sender;

    /**
     * This request's type.
     */
    private Type requestType;

    /**
     * This request's argument, if any.
     * May be an integer, or a Task instance.
     */
    private Object arg = null;

    private Request(InetSocketAddress sender, Type type, Object arg) {
        this.requestType = type;
    }

    public static Request makeSuccessorRequest(InetSocketAddress sender, int key) {
        return new Request(sender, Type.SUCCESSOR_OF, key);
    }

    public static Request makePredecessorRequest(InetSocketAddress sender, int key) {
        return new Request(sender, Type.PREDECESSOR_OF, key);
    }

    public static Request makeIthFingerRequest(InetSocketAddress sender, int ith) {
        return new Request(sender, Type.ITH_FINGER, ith);
    }

    public static Request makeTaskRequest(InetSocketAddress sender, AdversarialSearchTask task) {
        return new Request(sender, Type.TASK, task);
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return requestType;
    }

    public Object getArg() {
        return arg;
    }

    public InetSocketAddress getSenderAddress() {
        return sender;
    }

}

// NOTE maybe rethink necessity of having sender address, socket connection is already bi-directional
