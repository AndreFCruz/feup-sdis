package service;

import network.Key;
import remote.RemotePeer;

import java.rmi.RemoteException;
import java.util.Scanner;

public class Authentication {

    public static void login(RemotePeer stub) throws RemoteException {
        String[] result;

        do {
            result = getCredentials();
        } while (!checkLogin(stub, result[0], result[1]));

    }


    private static String[] getCredentials() {
        String[] result = new String[2];

        Scanner input1 = new Scanner(System.in);
        System.out.println("Enter Username : ");
        result[0] = input1.next();

        Scanner input2 = new Scanner(System.in);
        System.out.println("Enter Password : ");
        result[1] = input2.next();

        return result;
    }

    private static boolean checkLogin(RemotePeer stub, String username, String password) throws RemoteException {
        Key pass = (Key) stub.get(Key.fromObject(username));

        if(pass == null) {
            System.out.println("User not exists! Creating one...");
            stub.put(Key.fromObject(username), Key.fromObject(password));
            return true;
        } else if(pass.equals(Key.fromObject(password))) {
            System.out.println("Access Granted! Welcome!");
            return true;
        } else {
            System.out.println("Invalid Password!");
            return false;
        }

    }
}
