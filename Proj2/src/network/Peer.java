package network;

import task.AdversarialSearchTask;

import java.util.concurrent.Future;

public interface Peer extends ChordNode, RemotePeer {

    /**
     * Handles a given Task.
     * Actually performs the task in the current peer,
     *  and reports back to the initiator peer.
     * TODO may re-partition request through peers (?).
     * @param task the given task.
     * @return the value of the given task
     */
    Future<Integer> handleTask(AdversarialSearchTask task);

}
