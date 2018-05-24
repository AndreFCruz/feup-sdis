package service;

import network.RemotePeer;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;


// TODO finish implementing this class
public class InitClient implements Runnable {

    private Map<String, Runnable> handlers;
    private RemotePeer stub;

    private String action;
    private String operand;

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java InitClient <peer_address> <action> <operand>");
            return;
        }

        //host:port/name
        String[] peer_ap = Utils.parseRMI(false, args[0]);
        if (peer_ap == null)
            return;

        String action = args[1];
        String operand = args.length > 2 ? args[2] : null;

        InitClient app = new InitClient(action, operand);
        new Thread(app).start();
    }

    InitClient(String action, String operand) {
        this.action = action;
        this.operand = operand;

        handlers = new HashMap<>();
        handlers.put("STATUS", this::handleStatus);
    }

    @Override
    public void run() {
        initiateRMIStub();
        handlers.get(action).run();
    }

    private void initiateRMIStub() {
        try { // TODO getRegistry
//            Registry registry = Utils.getRegistry(peer_ap);
//            stub = (RemotePeer) registry.lookup(peer_ap[2]);
        } catch (Exception e) {
            System.err.println("Error when opening RMI stub");
            e.printStackTrace();
        }
    }

    void handleStatus() {
        try {
            System.out.println(stub.getStatus());
        } catch (RemoteException e) {
            System.err.println("Client exception: " + e.toString());
        }
    }

}

