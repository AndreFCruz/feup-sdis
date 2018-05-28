package network.threads;

import network.ChordNode;
import network.Key;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Confirms node's fingers are valid.
 */
public class FixFingers extends RecurrentTask {

    private static final int INTERVAL = 5000;

    private ChordNode node;

    public FixFingers(ChordNode node) {
        super(INTERVAL);
        this.node = node;
    }

    /**
      * Periodically checks the validity of the node's finger (except the node's successor, i=0)
      */
    @Override
    public void run() {
        for (int i = 1; i < Key.KEY_SIZE; i++) {
            Key ithStart = node.getKey().shift(i);
            InetSocketAddress ithFinger = node.findSuccessor(ithStart);
            node.setIthFinger(i, ithFinger);
        }
    }

}
