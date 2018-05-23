package network;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.Remote;

/**
 * A Node in a Chord - Distributed Hash Table.
 */
public interface ChordNode extends Remote {


    /**
     * Gets this node's id/key.
     * @return this node's id/key.
     */
    Key getKey();

    /**
     * Gets this node's address (ip:port).
     * @return this node's address.
     */
    InetSocketAddress getAddress();

    /**
     * Gets this node's successor.
     * @return the address of this node's successor.
     */
    InetSocketAddress getSuccessor();

    /**
     * Gets this node's predecessor.
     * @return the address of this node's predecessor.
     */
    InetSocketAddress getPredecessor();

    /**
     * Fetches the Object with the specified key.
     * @param key the Object's key.
     * @param <T> the type of the Object (must be serializable).
     * @return the Object with the specified key, if found.
     */
    <T extends Serializable> T lookup(Key key);

    /**
     * Puts the given object in this node's persistent memory.
     * @param key the Object's key.
     * @param obj the Object to be stored.
     */
    void put(Key key, Serializable obj);

    /**
     * Gets the ith finger on this node's finger table.
     * The ith entry of node n will contain:
     *  successor((n + 2^(i-1)) mod 2^m), m = 32
     * @param i the finger's position in this node's finger table.
     * @return the finger's address.
     */
    InetSocketAddress getIthFinger(int i);

    /**
     * Sets the ith finger of this node.
     * @param i the position on the finger table.
     * @param address the finger's address.
     */
    void setIthFinger(int i, InetSocketAddress address);

    /**
     * Sets this node's predecessor.
     * @param newPredecessor the new predecessor.
     */
    void setPredecessor(InetSocketAddress newPredecessor);

    /**
     * Creates a new Chord Ring.
     * The node's successor is set to the node itself.
     */
    void create();

    /**
     * Join a Chord Ring through the provided contact Node.
     * @param contact the address of the join contact.
     * @return whether join was successful.
     */
    boolean join(InetSocketAddress contact);

    /**
     * Causes this node to leave the Chord ring.
     */
    void leave();

    /**
     * Notify the given node that this node is its' predecessor.
     * @param successor this node's successor.
     * @return whether notify was successful.
     */
    boolean notify(InetSocketAddress successor);

    /**
     * Received notification signaling this node's predecessor.
     * @param predecessor this node's predecessor.
     */
    void notified(InetSocketAddress predecessor);

    /**
     * Find key's successor (the node responsible for the given key/id).
     * A successor of key is the first node whose id equals or follows key.
     * Every key is stored in its successor node.
     * @param key a key in the hash table.
     * @return the corresponding address.
     */
    InetSocketAddress findSuccessor(Key key);


}
