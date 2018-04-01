package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getIPV4Address() {
        String IP = null;

        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return IP;
    }

    public static String[] parseRMI(boolean Server, String accessPoint) {
        Pattern rmiPattern;
        if (Server) {
            rmiPattern = Pattern.compile("//([\\w.]+)(?::(\\d+))?/(\\w+)?");
        } else {
            rmiPattern = Pattern.compile("//([\\w.]+)(?::(\\d+))?/(\\w+)");
        }

        Matcher m = rmiPattern.matcher(accessPoint);
        String[] peer_ap = null;

        if (m.find()) {
            peer_ap = new String[]{m.group(1), m.group(2), m.group(3)};
        } else {
            Log.logError("Invalid Access Point!");
        }

        return peer_ap;
    }

    public static Registry getRegistry(String[] serviceAccessPoint) {
        Registry registry = null;
        // Bind the remote object's stub in the registry

        try {
            if (serviceAccessPoint[1] == null) {
                if (serviceAccessPoint[0] == "localhost") {

                    registry = LocateRegistry.getRegistry();

                } else {
                    registry = LocateRegistry.getRegistry(serviceAccessPoint[0]);
                }
            } else {
                if (serviceAccessPoint[0] == "localhost") {
                    registry = LocateRegistry.getRegistry(serviceAccessPoint[1]);
                } else {
                    registry = LocateRegistry.getRegistry(serviceAccessPoint[0], Integer.parseInt(serviceAccessPoint[1]));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return registry;
    }

}
