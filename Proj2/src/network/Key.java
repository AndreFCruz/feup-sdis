package network;

import java.io.*;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Key implements Serializable {

    private static final long serialVersionUID = 100L;

    public static final int KEY_SIZE = 32;

    public static final int MINIMUM = 0;

    public static final int MAXIMUM = (int) Math.pow(2, KEY_SIZE);

    private final long key;

    private Key(final long key) {
        if (key < MINIMUM) {
            throw new IllegalArgumentException("Key cannot be smaller than " + MINIMUM);
        }

        this.key = key % MAXIMUM;
    }

    private Key(final int key) {
        this(key & 0x00000000ffffffffL);
    }

    public static Key fromAddress(InetSocketAddress address) {
        return address == null ? null : new Key(hashSocketAddress(address));
    }

    public static Key fromObject(Object obj) {
        if (obj == null) return null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] data = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                // ignore close exception
            }
        }

        return new Key(hashData(data).hashCode());
    }

    private static int hashSocketAddress(InetSocketAddress address) {
        String ip = address.getAddress().getHostAddress();
        String port = Integer.toString(address.getPort());
        byte[] data = (ip+port).getBytes();
        return hashData(data).hashCode();
    }

    private static String hashData(byte[] data) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        messageDigest.update(data);
        String encryptedString = new String(messageDigest.digest());

        return encryptedString;
    }

    /**
     * Is key in range ]lower, upper], meaning upper is responsible for this key
     *  (if the provided keys correspond to the closest nodes to this key).
     * @param lower exclusive lower bound
     * @param upper inclusive upper bound
     * @return whether key is in range ]lower, upper]
     */
    public boolean isBetween(final Key lower, final Key upper) {
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
        return (int) this.key;
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
