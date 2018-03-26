package service;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

    public TestApp() {
    }

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            IService stub = (IService) registry.lookup("Hello");
            String response = stub.restore("Addeus");
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
