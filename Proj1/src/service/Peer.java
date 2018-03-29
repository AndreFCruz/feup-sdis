package service;

import channels.Channel;
import channels.Channel.ChannelType;
import channels.MChannel;
import channels.MDBChannel;
import channels.MDRChannel;
import filesystem.*;
import network.Handler;
import network.Message;
import protocols.PeerData;
import protocols.initiators.BackupInitiator;
import protocols.initiators.RestoreInitiator;
import utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static protocols.ProtocolSettings.MAX_SYSTEM_MEMORY;


public class Peer implements RemoteBackupService {

    /**
     * Handler and Dispatcher for received messages
     */
    private Handler dispatcher;

    /**
     * Executor service responsible for scheduling delayed responses
     * and performing all sub-protocol tasks (backup, restore, ...).
     */
    private ScheduledExecutorService executor;

    private Map<ChannelType, Channel> channels;

    private SystemManager systemManager;

    private Database database;

    private PeerData peerData;

    private int id;
//    private String protocolVersion;
//    private String serverAccessPoint;
//    private RemoteBackupService stub;
//

    public static void main(String args[]) {

//		if (args.length != 2) {
//			System.out.println("Usage: java Peer <mc:port> <mdb:port> <mdr:port>");
//			return;
//		}

        String[] mcAddress = args[1].split(":");
        String[] mdbAddress = args[2].split(":");
        String[] mdrAddress = args[3].split(":");

        // Flag needed for systems that use IPv6 by default
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            Peer obj = new Peer(Integer.parseInt(args[0]), mcAddress, mdbAddress, mdrAddress);
            RemoteBackupService stub = (RemoteBackupService) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(args[0], stub);
            //registry.bind(args[0], stub);

            Log.logWarning("Server ready");
        } catch (Exception e) {
            Log.logError("Server exception: " + e.toString());
            e.printStackTrace();
        }


    }

    public Peer(int id, String[] mcAddress, String[] mdbAddress, String[] mdrAddress) {
        this.id = id;

        setupChannels(mcAddress, mdbAddress, mdrAddress);
        setupDispatcher();

        systemManager = new SystemManager(this, MAX_SYSTEM_MEMORY);
        database = systemManager.getDatabase();

        executor = new ScheduledThreadPoolExecutor(10);

        Log.logWarning("Peer " + id + " online!");
    }

    private void setupDispatcher() {
        peerData = new PeerData();
        dispatcher = new Handler(this);
        new Thread(dispatcher).start();
    }

    private void setupChannels(String[] mcAddress, String[] mdbAddress, String[] mdrAddress) {
        Channel mc = new MChannel(this, mcAddress[0], mcAddress[1]);
        Channel mdb = new MDBChannel(this, mdbAddress[0], mdbAddress[1]);
        Channel mdr = new MDRChannel(this, mdrAddress[0], mdrAddress[1]);

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(mdr).start();

        channels = new HashMap<>();
        channels.put(ChannelType.MC, mc);
        channels.put(ChannelType.MDB, mdb);
        channels.put(ChannelType.MDR, mdr);
    }

    public void sendDelayedMessage(ChannelType channelType, Message message, long delay, TimeUnit unit) {
        executor.schedule(() -> {
            try {
                sendMessage(channelType, message);
            } catch (IOException e) {
                Log.logError("Error sending message to channel " + channelType + " - " + message.getHeaderAsString());
            }
        }, delay, unit);
    }

    public void sendMessage(ChannelType channelType, Message message) throws IOException {
        Log.logWarning("S: " + message.getHeaderAsString() + "|");

        channels.get(channelType).sendMessage(message.getBytes());
    }

    public Channel getChannel(ChannelType channelType) {
        return channels.get(channelType);
    }

    @Override
    public String backup(File file, int replicationDegree) {
        executor.execute(new BackupInitiator("1.0", file, replicationDegree, this));
        return "backup command ok";
    }

    @Override
    public boolean restore(String pathname) {
        final Future handler;
        try {
            handler = executor.submit(new RestoreInitiator("1.0", pathname, this));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.logError("Failed RestoreInitiator");
            return false;
        }

        executor.schedule(() -> {
            handler.cancel(true);
            Log.logWarning("RestoreInitiator was killed for lack of chunks.");
        }, 10, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public void delete(String pathname) {

    }

    @Override
    public void reclaim(int space) {

    }

    @Override
    public void state() {

    }

    public int getID() {
        return id;
    }

    public String getPath(String path) {
        String pathname;

        switch (path) {
            case "chunks":
                pathname = systemManager.getChunksPath();
                break;
            case "restores":
                pathname = systemManager.getRestoresPath();
                break;
            default:
                pathname = "";
                break;

        }
        return pathname;
    }

    public void addMsgToHandler(byte[] data, int length) throws IOException {
        dispatcher.pushMessage(data, length);
    }

    public void addFileToDB(String pathName, FileInfo fileInfo) {
        database.addRestorableFile(pathName, fileInfo);
    }

    public FileInfo getFileFromDB(String pathName) {
        return database.getFileInfo(pathName);
    }

    public void addChunkToDB(ChunkInfo chunkInfo) {
        database.addChunk(chunkInfo);
    }

    public boolean hasChunkFromDB(String fileID, int chunkNo) {
        return database.hasChunk(fileID, chunkNo);
    }

    public void setRestoring(boolean flag, String fileID) {
        peerData.setFlagRestored(flag, fileID);
    }


    public boolean hasRestoreFinished(String pathName, String fileID) {
        int numChunks = database.getNumChunks(pathName);
        int chunksRestored = peerData.getChunksRestoredSize(fileID);

        return numChunks == chunksRestored;
    }

    public boolean getFlagRestored(String fileID) {
        return peerData.getFlagRestored(fileID);
    }

    public void addChunkToRestore(Chunk chunk) {
        peerData.addChunksRestored(chunk);
    }

    public ConcurrentMap<Integer, Chunk> getChunksRestored(String fileID) {
        return peerData.getChunksRestored(fileID);
    }

    public PeerData getPeerData() {
        return peerData;
    }
}
