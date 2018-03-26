package service;

import java.io.File;
import java.rmi.NotBoundException;
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
    private IService stub;

    public static void main(String[] args) {
        if(args.length < 2 || args.length > 3) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        String peer_ap = args[0]; // peer access point
        String sub_protocol = args[1];
        String operand1 = args[2];
        String operand2 = args.length > 2 ? args[3] : null;

        TestApp app = new TestApp(peer_ap, sub_protocol, operand1, operand2);
        app.handleRequest();
    }

    public TestApp(String peer_ap, String sub_protocol, String opnd_1, String opnd_2) {
        this.peer_ap = peer_ap;
        this.sub_protocol = sub_protocol;
        this.opnd_1 = opnd_1;
        this.opnd_2 = opnd_2;

        handlers = new HashMap<>();
        handlers.put("BACKUP", this::handleBackup );
        handlers.put("RESTORE", this::handleRestore );
        handlers.put("DELETE", this::handleDelete );
        handlers.put("RECLAIM", this::handleReclaim );
        handlers.put("STATE", this::handleState );

    }

    private void handleRequest() {
        initiateRMIStub();

        String response = handlers.get(sub_protocol)
        System.err.println("response: " + response);
        // Do something with response ?
    }

    private void initiateRMIStub() {
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            stub = (IService) registry.lookup(peer_ap);
        } catch (Exception e) {
            System.err.println("Error when opening RMI stub");
            e.printStackTrace();
        }
    }

    private String handleBackup() {
        File file = new File(this.opnd_1);
        System.out.println("Backing up file at \"" + file.getAbsolutePath() + "\"");

        String response = null;
        try {
            response = stub.backup(file, Integer.parseInt(this.opnd_2));
        } catch (RemoteException e) {
            System.err.println("Client exception: " + e.toString());
        }

        return response;
    }

    private void handleDelete() {
        // TODO
    }

    private void handleRestore() {
        // TODO
    }


    private void handleState() {
        // TODO
    }

    private void handleReclaim() {
        // TODO
    }

}
