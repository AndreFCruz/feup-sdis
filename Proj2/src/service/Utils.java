package service;

import network.Log;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {
    // TODO mudar o parseRMI, nao precisa do ultimo digito
    static String[] parseRMI(boolean Server, String accessPoint) {
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

    static Registry getRegistry(String[] serviceAccessPoint) {
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
