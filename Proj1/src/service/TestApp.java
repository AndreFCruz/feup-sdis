package service;

import utils.Log;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class TestApp {

    private String peer_ap;
    private String sub_protocol;
    private String opnd_1;
    private String opnd_2;

    private Map<String, Runnable> handlers;
    private Service stub;

    public TestApp(String peer_ap, String sub_protocol, String opnd_1, String opnd_2) {
        this.peer_ap = peer_ap;
        this.sub_protocol = sub_protocol;
        this.opnd_1 = opnd_1;
        this.opnd_2 = opnd_2;

        handlers = new HashMap<>();
        handlers.put("BACKUP", this::handleBackup);
        handlers.put("RESTORE", this::handleRestore);
        handlers.put("DELETE", this::handleDelete);
        handlers.put("RECLAIM", this::handleReclaim);
        handlers.put("STATE", this::handleState);

    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            Log.logWarning("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        String peer_ap = args[0]; // peer access point
        String sub_protocol = args[1];
        String operand1 = args[2];
        String operand2 = args.length > 3 ? args[3] : null;

        TestApp app = new TestApp(peer_ap, sub_protocol, operand1, operand2);
        app.handleRequest();
    }

    private void handleRequest() {
        initiateRMIStub();

        handlers.get(sub_protocol).run();
        // Do something with response ?
    }

    private void initiateRMIStub() {
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            stub = (Service) registry.lookup(peer_ap);
        } catch (Exception e) {
            Log.logError("Error when opening RMI stub");
            e.printStackTrace();
        }
    }

    private void handleBackup() {
        File file = new File(this.opnd_1);
        Log.logWarning("BACKING UP file at \"" + file.getAbsolutePath() + "\"");

        try {
            stub.backup(file, Integer.parseInt(this.opnd_2));
        } catch (RemoteException e) {
            Log.logError("Client exception: " + e.toString());
        }
    }

    private void handleDelete() {
        Log.logWarning("DELETING file \"" + opnd_1 + "\"");

        try {
            stub.delete(this.opnd_1);
        } catch (RemoteException e) {
            Log.logError("Client exception: " + e.toString());
        }
    }

    private void handleRestore() {
        Log.logWarning("RESTORING file \"" + opnd_1 + "\"");

        try {
            stub.restore(opnd_1);
        } catch (RemoteException e) {
            Log.logError("Client exception: " + e.toString());
        }
    }

    private void handleReclaim() {
        Log.logWarning("RECLAIMING disk space: \"" + opnd_1 + "\"");

        try {
            stub.reclaim(Integer.parseInt(opnd_1));
        } catch (RemoteException e) {
            Log.logError("Client exception: " + e.toString());
        }
    }

    private void handleState() {
        Log.logWarning("This is my state :D");
        try {
            stub.state();
        } catch (RemoteException e) {
            Log.logError("Client exception: " + e.toString());
        }
    }

}
