package network.threads;

import network.ChordNode;
import network.Key;
import network.Message;

import java.net.InetSocketAddress;

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
        Message request = Message.makeRequest(Message.Type.SUCCESSOR, null);
        InetSocketAddress candidate = dispatcher.requestAddress(successor, request);

        Key succKey = Key.fromAddress(successor);
        Key candKey = Key.fromAddress(candidate);

        // If there's a peer between this peer and its successor,
        // then that peer is this peer's new successor.
        if (candKey.isBetween(node.getKey(), succKey)) {
            node.setIthFinger(0, candidate);
        }

        node.notify(node.getSuccessor());
    }
}
