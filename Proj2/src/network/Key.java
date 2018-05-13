package network;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Key implements Serializable {

    private static final long serialVersionUID = 100L;

    public static final int KEY_SIZE = 32;

    private final int key;

    private Key(int key) {
        this.key = key;
    }

    public static Key fromAddress(InetSocketAddress address) {
        return new Key(address.hashCode());
    }

    public static Key fromObject(Object obj) {
        return new Key(obj.hashCode());
    }

    /**
     * Is key in range ]lower, upper], meaning upper is responsible for this key
     *  (if the provided keys correspond to the closest nodes to this key).
     * @param lower exclusive lower bound
     * @param upper inclusive upper bound
     * @return whether key is in range ]lower, upper]
     */
    public boolean isBetween(Key lower, Key upper) {
        if (lower.key < upper.key) {
            return this.key > lower.key && this.key <= upper.key;
        } else {
            return this.key > lower.key || this.key <= upper.key;
        }
    }

    /**
     * @return key + 2^bits
     */
    public Key shift(final int bits) {
        return new Key(this.key + 1 << bits);
    }

    @Override
    public int hashCode() {
        return this.key;
    }

    @Override
    public boolean equals(final Object object) {
        if (! (object instanceof Key) )
            return false;

        return this == object || this.key == ((Key) object).key;
    }

    @Override
    public String toString() {
        return this.key + "";
    }
}
