package network;

import java.net.InetSocketAddress;

/**
 * A Node in a Chord - Distributed Hash Table.
 */
public interface ChordNode {

    /**
     * Join a Chord Ring through the provided contact Node.
     * @param contact the address of the join contact.
     * @return whether join was successful.
     */
    boolean join(InetSocketAddress contact);

    /**
     * Notify the given node that this node is its' predecessor.
     * @param successor this node's successor.
     * @return whether notify was successful.
     */
    boolean notify(InetSocketAddress successor);

    /**
     * Received notification signaling this node's predecessor.
     * @param predecessor this node's predecessor.
     * @return whether notified was successful.
     */
    boolean notified(InetSocketAddress predecessor);

    /**
     * Find id's successor.
     * @param id a key in the hash table.
     * @return the corresponding address.
     */
    InetSocketAddress findSuccessor(int id);

}
