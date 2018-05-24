package network.threads;

import network.ChordNode;
import network.Key;
import network.Message;

import java.net.InetSocketAddress;

/**
 * Periodically checks whether node's predecessor is alive.
 */
public class CheckPredecessor extends RecurrentTask {

    private static final int INTERVAL = 2000;

    private ChordNode node;
    private MessageDispatcher dispatcher;

    public CheckPredecessor(ChordNode node, MessageDispatcher dispatcher) {
        super(INTERVAL);

        this.node = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        InetSocketAddress predecessor = node.getPredecessor();
        if (predecessor == null) {
            Log.log("Predecessor not set.");
            return;
        }

        Message request = Message.makeRequest(Message.Type.KEY, null, node.getAddress());
        Message response = dispatcher.sendRequest(predecessor, request);

        if (response == null)
            node.setPredecessor(null);
        else if ( response.getArg().equals(Key.fromAddress(predecessor)) )
            Log.log("Predecessor still lives :)");
        else
            System.err.println("Found predecessor but key was invalid.");
    }
}
