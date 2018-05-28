package service;

import network.Key;
import remote.RemotePeer;
import task.AdversarialSearchTask;
import task.GameState;
import task.MinimaxSearchTask;
import task.Player;
import task.tictactoe.TicTacToe;
import task.tictactoe.TicTacToeBoard;
import task.tictactoe.TicTacToePlayer;
import task.tictactoe.TicTacToeState;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


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

    public static Scanner in = new Scanner(System.in); // the input Scanner

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
        handlers.put("GET", this::handleGet);
        handlers.put("PUT", this::handlePut);
        handlers.put("FIND_SUCCESSOR", this::handleFindSuccessor);
        handlers.put("TASK", this::handleTask);
        handlers.put("MATCH", this::handleMatch);
    }

    @Override
    public void run() {
        if (!initiateRMIStub())
            return;

        try {
            Authentication.login(stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Runnable runnable = handlers.get(action.toUpperCase());
        if (runnable != null)
            runnable.run();
        else
            System.err.println("No handler installed for action \"" + action + "\".");
    }

    private boolean initiateRMIStub() {
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
            return false;
        }

        return true;
    }

    private void handleStatus() {
        try {
            System.out.println(stub.getStatus());
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }
    }

    private void handleGet() {
        Key key = Key.fromObject(oper1);
        System.out.println("Searching for data with key: " + key + " (" + oper1 + ")");
        Serializable data = null;
        try {
            data = stub.get(key);
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }

        System.out.println("Data: " + data);
    }

    private void handlePut() {
        Key key = Key.fromObject(oper1);
        System.out.println("Storing data data with key: " + key + " (" + oper1 + ")");
        Serializable data = oper2;
        try {
            stub.put(key, data);
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }
    }

    private void handleFindSuccessor() {
        Key key = new Key(Integer.parseInt(oper1));
        System.out.println("Finding successor of key: " + key + " (" + oper1 + ")");
        InetSocketAddress address = null;
        try {
            address = stub.findSuccessor(key);
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }

        System.out.println("Successor's Address: " + address);
    }

    private void handleTask() {
        AdversarialSearchTask task = new MinimaxSearchTask(
                new TicTacToe(),
                new TicTacToeState(),
                new TicTacToePlayer("CROSSES", TicTacToeBoard.Cell.CROSS)
        );

        GameState result = null;
        try {
            result = stub.initiateTask(task);
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }

        System.out.println(result.getBoard().display());
        System.out.println("Task sent");
    }

    public void handleMatch() {
        TicTacToeBoard board = new TicTacToeBoard();

        System.out.println("Welcome to Distributed Tic-Tac-Toe!");
        System.out.println("Starting match...");

        System.out.println(board.display());

        boolean isOver = false;
        while(!isOver) {
            getInput(board);

            TicTacToeState state = new TicTacToeState(board, TicTacToeBoard.Cell.NOUGH);
            TicTacToe ttt = new TicTacToe();
            AdversarialSearchTask task = new MinimaxSearchTask(
                    ttt,
                    state,
                    new TicTacToePlayer("NOUGH", TicTacToeBoard.Cell.NOUGH)
            );

            board = makeRequest(task);

            if(ttt.isStateTerminal(state))
                isOver = true;
        }

        System.out.println("Game Over!");
    }

    private void getInput(TicTacToeBoard board) {
        boolean validInput = false;

        while (!validInput) {
            System.out.print("Player 'X', enter your move (row[1-3] column[1-3]): ");

            int row = in.nextInt() - 1;
            int col = in.nextInt() - 1;

            if (validInput(board, row, col)) {
                validInput = true;
                board.setCell(row, col, TicTacToeBoard.Cell.CROSS);
            } else
                System.out.print("Invalid move, try again...");
        }

        System.out.println(board.display());
    }

    private TicTacToeBoard makeRequest(AdversarialSearchTask task) {
        System.out.println("AI is thinking...");
        GameState result = null;
        try {
            result = stub.initiateTask(task);
        } catch (RemoteException e) {
            System.err.println("Remote Client exception: " + e.getMessage());
        }

        System.out.println(result.getBoard().display());
        return (TicTacToeBoard) result.getBoard();
    }

    private boolean validInput(TicTacToeBoard board, int row, int col) {
        return row >= 0 && row < TicTacToeBoard.N_ROWS
                && col >= 0 && col < TicTacToeBoard.N_COLS
                && board.isFreeCell(row, col);
    }
}

