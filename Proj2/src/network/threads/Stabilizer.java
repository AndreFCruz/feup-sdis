package network.threads;

import network.ChordNode;
import network.Key;
import network.Message;

import java.net.InetSocketAddress;

/**
 * Confirms node's successor is valid.
 */
public class Stabilizer extends RecurrentTask {

    private static final int INTERVAL = 1000;

    private ChordNode node;
    private MessageDispatcher dispatcher;

    public Stabilizer(ChordNode node, MessageDispatcher dispatcher) {
        super(INTERVAL);
        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        InetSocketAddress successor = node.getSuccessor();
//        if (successor == node.getAddress()) {
//            // TODO rethink this situation
//            return;
//        }

        Message request = Message.makeRequest(Message.Type.PREDECESSOR, null, node.getAddress());
        InetSocketAddress candidate = dispatcher.requestAddress(successor, request);
        if (candidate == null) {
            System.out.println("Stabilizer: Predecessor of successor is NULL.");
            return;
        }

        Key succKey = Key.fromAddress(successor);
        Key candKey = Key.fromAddress(candidate);

        // If there's a peer between this peer and its successor,
        // then that peer is this peer's new successor.
        if (candKey.isBetween(node.getKey(), succKey) || node.getKey() == succKey) {
            node.setIthFinger(0, candidate);
        }

        node.notify(node.getSuccessor());
    }
}
