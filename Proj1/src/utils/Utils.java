package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static final String CRLF = "" + (char) 0x0D + (char) 0x0A;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hash(String msg) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.logError("Hash algorithm not found: " + e.getMessage());
            return null;
        }

        byte[] hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
        String hashedID = bytesToHex(hash);
        Log.logWarning("" + hash.length + " : " + hashedID);
        return hashedID;
    }

}
