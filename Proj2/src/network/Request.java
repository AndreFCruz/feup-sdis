package network;

import task.AdversarialSearchTask;

import java.io.Serializable;

public class Request implements Serializable {
    public enum Type {
        SUCCESSOR,
        PREDECESSOR,
        ITH_FINGER,
        TASK
    }

    /**
     * This request's type.
     */
    private Type requestType;

    /**
     * This request's argument, if any.
     * May be an integer (for ith finger), or a Task instance.
     */
    private Object arg = null;

    private Request(Type type) {
        this.requestType = type;
    }

    public static Request makeSuccessorRequest() {
        return new Request(Type.SUCCESSOR);
    }

    public static Request makePredecessorRequest() {
        return new Request(Type.PREDECESSOR);
    }

    public static Request makeIthFingerRequest(int ith) {
        Request req = new Request(Type.ITH_FINGER);
        req.arg = ith;
        return req;
    }

    public static Request makeTaskRequest(AdversarialSearchTask task) {
        Request req = new Request(Type.TASK);
        req.arg = task;
        return req;
    }

    public Type getRequestType() {
        return requestType;
    }

    public Object getArg() {
        return arg;
    }

}
