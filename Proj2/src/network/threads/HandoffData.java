package network.threads;

import network.ChordNode;

public class HandoffData extends RecurrentTask {

    private static final int INTERVAL = 2000;

    private ChordNode node;

    public HandoffData(ChordNode node) {
        super(INTERVAL);
        this.node = node;
    }

    /**
      * Hands off data stored in the node for which it is not longer responsible
      */
    @Override
    public void run() {
        node.handoff();
    }

}
