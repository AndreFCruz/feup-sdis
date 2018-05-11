package network;

public interface Peer {

    /**
     * Initiates a Request to the peer-to-peer network.
     * Partitions task in n tasks, and distributes said tasks to
     *  peers in the network, according to the task's hash and
     *  following the Chord implementation.
     * @param request the client's request.
     */
    void initiateRequest(Request request);

    /**
     * Handles a given Request.
     * Actually performs the task in the current peer,
     *  and reports back to the initiator peer.
     * TODO may re-partition request through peers (?).
     * @param request the given request.
     */
    void handleRequest(Request request);
}
