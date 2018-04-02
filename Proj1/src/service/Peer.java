package service;

import channels.Channel;
import channels.Channel.ChannelType;
import channels.MChannel;
import channels.MDBChannel;
import channels.MDRChannel;
import filesystem.Database;
import filesystem.SystemManager;
import network.Handler;
import network.Message;
import protocols.PeerData;
import protocols.initiators.*;
import utils.Log;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static protocols.ProtocolSettings.*;
import static utils.Utils.getRegistry;
import static utils.Utils.parseRMI;

public class Peer implements RemoteBackupService {

    private final String protocolVersion;
    private final int id;
    private final String[] serverAccessPoint;
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

    public Peer(String protocolVersion, int id, String[] serverAccessPoint, String[] mcAddress, String[] mdbAddress, String[] mdrAddress) {
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;

        systemManager = new SystemManager(this, MAX_SYSTEM_MEMORY);
        database = systemManager.getDatabase();

        setupChannels(mcAddress, mdbAddress, mdrAddress);
        setupDispatcher();

        executor = new ScheduledThreadPoolExecutor(10);

        sendUPMessage();

        Log.logWarning("Peer " + id + " online!");
    }

    public static void main(String args[]) {
        if (args.length != 6) {
            System.out.println("Usage: java -classpath bin service.Peer" +
                    " <protocol_version> <server_id> <service_access_point>" +
                    " <mc:port> <mdb:port> <mdr:port>");
            return;
        }

        String protocolVersion = args[0];
        int serverID = Integer.parseInt(args[1]);

        //Parse RMI address
        //host/ or   //host:port/
        String[] serviceAccessPoint = parseRMI(true, args[2]);
        if (serviceAccessPoint == null) {
            return;
        }

        String[] mcAddress = args[3].split(":");
        String[] mdbAddress = args[4].split(":");
        String[] mdrAddress = args[5].split(":");

        // Flag needed for systems that use IPv6 by default
        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            Peer obj = new Peer(protocolVersion, serverID, serviceAccessPoint, mcAddress, mdbAddress, mdrAddress);
            RemoteBackupService stub = (RemoteBackupService) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = getRegistry(serviceAccessPoint);
            registry.rebind(args[1], stub); //Only use rebind for development purposes
            //registry.bind(args[1], stub);

            Log.logWarning("Server ready");
        } catch (Exception e) {
            Log.logError("Server exception: " + e.toString());
            e.printStackTrace();
        }
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

    public Future sendDelayedMessage(ChannelType channelType, Message message, long delay, TimeUnit unit) {
        return executor.schedule(() -> {
            try {
                sendMessage(channelType, message);
            } catch (IOException e) {
                Log.logError("Error sending message to channel " + channelType + " - " + message.getHeaderAsString());
            }
        }, delay, unit);
    }

    public void sendMessage(ChannelType channelType, Message message) throws IOException {
        Log.logWarning("S: " + message.toString());

        channels.get(channelType).sendMessage(message.getBytes());
    }

    public Channel getChannel(ChannelType channelType) {
        return channels.get(channelType);
    }

    @Override
    public String backup(String pathname, int replicationDegree) {
        executor.execute(new BackupInitiator(protocolVersion, pathname, replicationDegree, this));
        return "backup command ok";
    }

    @Override
    public boolean restore(String pathname) {
        final Future handler;
        handler = executor.submit(new RestoreInitiator(protocolVersion, pathname, this));

        executor.schedule(() -> {
            if (handler.cancel(true)) {
                Log.logWarning("RestoreInitiator was killed for lack of chunks.");
            }
        }, 20, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public void delete(String pathname) {
        executor.execute(new DeleteInitiator(protocolVersion, pathname, this));
    }

    @Override
    public void reclaim(int space) {
        systemManager.setMaxMemory(space);
        executor.execute(new ReclaimInitiator(protocolVersion, this));
    }

    @Override
    public void state() {
        executor.execute(new RetrieveStateInitiator(protocolVersion, this));
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

    private void sendUPMessage() {
        if (isPeerCompatibleWithEnhancement(ENHANCEMENT_DELETE, this)) {
            //wait for server ready
            String[] args = {
                    getVersion(),
                    Integer.toString(getID()),
            };

            Message msg = new Message(Message.MessageType.UP, args);

            try {
                sendMessage(Channel.ChannelType.MC, msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void addMsgToHandler(byte[] data, int length) {
        dispatcher.pushMessage(data, length);
    }

    public byte[] loadChunk(String fileID, int chunkNo) {
        return systemManager.loadChunk(fileID, chunkNo);
    }

    public void setRestoring(boolean flag, String fileID) {
        peerData.setFlagRestored(flag, fileID);
    }

    public boolean hasRestoreFinished(String pathName, String fileID) {
        int numChunks = database.getNumChunksByFilePath(pathName);
        int chunksRestored = peerData.getChunksRestoredSize(fileID);

        Log.log("numChunks: " + numChunks);
        Log.log("chunksRestored: " + chunksRestored);

        return numChunks == chunksRestored;
    }

    public PeerData getPeerData() {
        return peerData;
    }

    public Database getDatabase() {
        return database;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }

    public String getVersion() {
        return protocolVersion;
    }
}
