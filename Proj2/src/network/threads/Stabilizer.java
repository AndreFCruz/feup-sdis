package network.threads;

import network.ChordNode;
import network.Key;
import network.Message;

import java.net.InetSocketAddress;

public class Stabilizer extends ThreadImpl {

    private static final int INTERVENTIONS_INTERVAL = 1000;

    private ChordNode node;
    private MessageDispatcher dispatcher;

    public Stabilizer(ChordNode node, MessageDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void act() {
        InetSocketAddress successor = node.getSuccessor();
        Message request = Message.makeRequest(Message.Type.SUCCESSOR, null);
        InetSocketAddress candidate = dispatcher.requestAddress(successor, request);

        Key succKey = Key.fromAddress(successor);
        Key candKey = Key.fromAddress(candidate);

        // If there's a peer between this peer and its successor,
        // then that peer is this peer's new successor.
        if (candKey.isBetween(node.getId(), succKey)) {
            node.setIthFinger(0, candidate);
        }

        node.notify(node.getSuccessor());

        try {
            Thread.sleep(INTERVENTIONS_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void terminate() {

    }

}
