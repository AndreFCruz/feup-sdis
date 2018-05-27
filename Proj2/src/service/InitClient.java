package service;

import remote.Authentication;
import remote.RemotePeer;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;


public class InitClient implements Runnable {

    private Map<String, Runnable> handlers;
    private RemotePeer stub;

    /**
     * IP and port in which target peer is operating
     */
    private String[] remoteAP;

    private String action;

    // Operands useful for GET and PUT operations on the Distributed Hash Map
    private String oper1;
    private String oper2;

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Usage: java InitClient <peer_address:port> <action> [<oper1> <oper2>]");
            return;
        }

        //host:port/name
        String[] remoteInterfaceAP = Utils.parseSocketAddress(args[0]);
        if (remoteInterfaceAP == null)
            return;

        String action = args[1];
        String oper1 = args.length > 2 ? args[2] : null;
        String oper2 = args.length > 3 ? args[3] : null;

        InitClient app = new InitClient(remoteInterfaceAP, action, oper1, oper2);
        new Thread(app).start();
    }

    InitClient(String[] remoteAP, String action, String operand1, String operand2) {
        this.remoteAP = remoteAP;
        this.action = action;
        this.oper1 = operand1;
        this.oper2 = operand2;

        handlers = new HashMap<>();
        handlers.put("STATUS", this::handleStatus);
    }

    @Override
    public void run() {
        initiateRMIStub();

        try {
            Authentication.login(stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Runnable runnable = handlers.get(action);
        if (runnable != null)
            runnable.run();
        else
            System.err.println("No handler installed for action \"" + action + "\".");
    }

    private void initiateRMIStub() {
        InetSocketAddress address = null;
        if (remoteAP[0].equals("localhost")) {
            address = new InetSocketAddress(Utils.getLocalIp(), Integer.parseInt(remoteAP[1]));
        } else {
            address = new InetSocketAddress(remoteAP[0], Integer.parseInt(remoteAP[1]));
        }
        final String registryName = Utils.getNameFromAddress(address);
        System.out.println("Looking up peer with registry name \"" + registryName + "\"");

        try {
            Registry registry = Utils.getRegistry(remoteAP[0]);
            stub = (RemotePeer) registry.lookup(registryName);
        } catch (Exception e) {
            System.err.println("Error when fetching RMI stub: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void handleStatus() {
        try {
            System.out.println(stub.getStatus());
        } catch (RemoteException e) {
            System.err.println("Client exception: " + e.getMessage());
        }
    }

}

