package network.threads;

import network.ChordNode;
import network.Key;
import network.Message;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Stabilizer extends Thread {

    private static final int INTERVAL = 1000;

    private ChordNode node;
    private MessageDispatcher dispatcher;
    private Timer timer;

    public Stabilizer(ChordNode node, MessageDispatcher dispatcher) {
        this.node = node;
        this.dispatcher = dispatcher;

        Stabilizer thisStabilizer = this;
        this.timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        thisStabilizer.stabilize();
                    }
                },
                INTERVAL,
                INTERVAL
        );
    }

    public void stabilize() {
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

    public void terminate() {
        timer.cancel();
    }

}
