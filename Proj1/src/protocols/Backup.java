package protocols;

import channels.Channel;
import filesystem.ChunkInfo;
import filesystem.SystemManager;
import filesystem.SystemManager.SAVE_STATE;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static filesystem.SystemManager.createFolder;
import static filesystem.SystemManager.saveFile;
import static protocols.ProtocolSettings.MAX_DELAY;

public class Backup implements Runnable, PeerData.MessageObserver {

    private Peer parentPeer;
    private Message request;

    private Random random;
    private Future handler = null;

    private int storedCount = 0;

    private ScheduledExecutorService scheduledExecutor;

    public Backup(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        this.random = new Random();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }


    @Override
    public void run() {
        int senderID = request.getSenderID();
        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();
        int replicationDegree = request.getReplicationDegree();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
//            Log.log("Ignoring backup of own files");
            return;
        }

        byte[] chunkData = request.getBody();

        String chunkPath = parentPeer.getPath("chunks") + "/" + fileID;
        createFolder(parentPeer.getPath("chunks") + "/" + fileID);

        parentPeer.getPeerData().attachStoredObserver(this);
        this.handler = scheduledExecutor.schedule(
                () -> saveChunk(fileID, chunkNo, replicationDegree, chunkData, chunkPath),
                this.random.nextInt(MAX_DELAY + 1),
                TimeUnit.MILLISECONDS
        );

        try {
            this.handler.wait();
            parentPeer.getPeerData().detachStoredObserver(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.log("Finished backup!");
    }

    private void saveChunk(String fileID, int chunkNo, int replicationDegree, byte[] chunkData, String chunkPath) {
        SAVE_STATE ret = SAVE_STATE.FAILURE;
        try {
            ret = saveFile(Integer.toString(chunkNo), chunkPath, chunkData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ret == SAVE_STATE.SUCCESS) {
            //save to database
            parentPeer.getDatabase().addChunk((new ChunkInfo(fileID, chunkNo, replicationDegree, chunkData.length)));
            sendSTORED(request);
            parentPeer.getDatabase().addChunk((new ChunkInfo(fileID, chunkNo, replicationDegree, chunkData.length)));
        } else { // Don't send STORED if chunk already existed (?)
            Log.logWarning("Chunk Backup: " + ret);
        }
    }

    private void sendSTORED(Message request) {
        String[] args = {
                request.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID(),
                Integer.toString(request.getChunkNo())
        };

        Message msg = new Message(Message.MessageType.STORED, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            Log.logError("Failed message construction");
            e.printStackTrace();
        }
    }

    @Override
    public void update(Message msg) {
        if (this.handler == null)
            return;
        if ( msg.getChunkNo() != request.getChunkNo() || (! msg.getFileID().equals(request.getFileID())) )
            return;

        storedCount += 1;
        if (storedCount >= request.getReplicationDegree()) {
            // Cancel if chunk's perceived replication fulfills requirements
            this.handler.cancel(true);
        }
    }
}
