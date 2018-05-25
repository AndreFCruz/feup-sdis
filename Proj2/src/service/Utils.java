package service;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {

    static String[] parseSocketAddress(String inputStr) {
        Pattern rmiPattern;// ^((?:[\w]+[\.]?)+):([\d]+)$
        rmiPattern = Pattern.compile("^((?:[\\w]+[\\.]?)+):([\\d]+)$");

        Matcher m = rmiPattern.matcher(inputStr);

        String[] address = null;
        if (m.find()) {
            address = new String[]{m.group(1), m.group(2)};
        } else {
            System.err.println("Invalid Access Point!");
        }

        return address;
    }

    static Registry getRegistry(String accessPointIP) {
        Registry registry = null;
        // Bind the remote object's stub in the registry

        try { // Using default port == 1099
            if (accessPointIP.equals("localhost")) {
                registry = LocateRegistry.getRegistry();
            } else {
                registry = LocateRegistry.getRegistry(accessPointIP);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return registry;
    }

    static InetAddress getLocalIp() {
        InetAddress localIP = null;
        try {
            localIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return localIP;
    }
}
