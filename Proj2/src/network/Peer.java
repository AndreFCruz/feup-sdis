package network;

import task.AdversarialSearchTask;

public interface Peer extends ChordNode {

    /**
     * Initiates a Message to the peer-to-peer network.
     * Partitions task in n tasks, and distributes said tasks to
     *  peers in the network, according to the task's hash and
     *  following the Chord implementation.
     * @param task the client's requested task.
     */
    void initiateTask(AdversarialSearchTask task);

    /**
     * Handles a given Task.
     * Actually performs the task in the current peer,
     *  and reports back to the initiator peer.
     * TODO may re-partition request through peers (?).
     * @param task the given task.
     * @return the value of the given task
     */
    int handleTask(AdversarialSearchTask task);
}
