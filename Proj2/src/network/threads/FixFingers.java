package network.threads;

import network.ChordNode;
import network.Key;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Confirms node's fingers are valid.
 */
public class FixFingers extends RecurrentTask {

    private static final int INTERVAL = 1000;

    private ChordNode node;
    private Random random;

    public FixFingers(ChordNode node) {
        super(INTERVAL);

        this.node = node;
        this.random = new Random();
    }

    @Override
    public void run() {
        // Periodically checks the validity of a random finger
        // except the node's successor (i=0)
        int randInt = random.nextInt(Key.KEY_SIZE - 1) + 1;
        Key ithStart = node.getKey().shift(randInt);
        InetSocketAddress ithFinger = node.findSuccessor(ithStart);
        node.setIthFinger(randInt, ithFinger);
    }

}
