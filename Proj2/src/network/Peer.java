package network;

import task.AdversarialSearchTask;

import java.util.concurrent.Future;

public interface Peer extends ChordNode {

    /**
     * Initiates a Message to the peer-to-peer network.
     * Partitions task in n tasks, and distributes said tasks to
     *  peers in the network, according to the task's hash and
     *  following the Chord implementation.
     * @param task the client's requested task.
     */
    AdversarialSearchTask initiateTask(AdversarialSearchTask task);

    /**
     * Handles a given Task.
     * Actually performs the task in the current peer,
     *  and reports back to the initiator peer.
     * @param task the given task.
     * @return the value of the given task
     */
    Future<Integer> handleTask(AdversarialSearchTask task);

    /**
     * Gracefully terminate this peer's resources.
     */
    void terminate();

    /**
     * Returns a formatted string displaying this peer's status.
     * @return a formatted string.
     */
    String getStatus();

}
