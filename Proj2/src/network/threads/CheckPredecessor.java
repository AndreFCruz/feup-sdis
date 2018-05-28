package network.threads;

import network.ChordNode;
import network.Key;
import network.Logger;
import network.Message;

import java.net.InetSocketAddress;

/**
 * Periodically checks whether node's predecessor is alive.
 */
public class CheckPredecessor extends RecurrentTask {
    /**
      * Periodicity of check operation
      */
    private static final int INTERVAL = 2000;

    private ChordNode node;
    private MessageDispatcher dispatcher;

    public CheckPredecessor(ChordNode node, MessageDispatcher dispatcher) {
        super(INTERVAL);

        this.node = node;
        this.dispatcher = dispatcher;
    }

    /**
      * Checks if the node's predecessor is still alive
      */
    @Override
    public void run() {
        InetSocketAddress predecessor = node.getPredecessor();
        if (predecessor == null) {
            Logger.log("Predecessor not set.");
            return;
        }

        Message request = Message.makeRequest(Message.Type.KEY, null, node.getAddress());
        Message response = dispatcher.sendRequest(predecessor, request);

        if (response == null)
            node.setPredecessor(null);
        else if ( response.getArg().equals(Key.fromAddress(predecessor)) )
            Logger.log("Predecessor still lives :)");
        else
            System.err.println("Found predecessor but key was invalid.");
    }
}
