package protocols;

import channels.Channel;
import filesystem.ChunkInfo;
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
import static protocols.ProtocolSettings.*;

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

        Log.logWarning("Starting backup!");
    }


    @Override
    public void run() {
        int senderID = request.getSenderID();
        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();
        int replicationDegree = request.getReplicationDegree();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
            return;
        }

        byte[] chunkData = request.getBody();

        String chunkPath = parentPeer.getPath("chunks") + "/" + fileID;
        createFolder(parentPeer.getPath("chunks") + "/" + fileID);

        if (isCompatibleWithEnhancement(ENHANCEMENT_BACKUP, request, parentPeer)) {
            handleEnhancedRequest(fileID, chunkNo, replicationDegree, chunkData, chunkPath);
        } else {
            handleStandardRequest(fileID, chunkNo, replicationDegree, chunkData, chunkPath);
        }

        Log.logWarning("Finished backup!");
    }

    private void handleStandardRequest(String fileID, int chunkNo, int replicationDegree, byte[] chunkData, String chunkPath) {
        boolean success = saveChunk(fileID, chunkNo, replicationDegree, chunkData, chunkPath);
        if (success) {
            sendDelayedSTORED(request);
        }
    }

    private void handleEnhancedRequest(String fileID, int chunkNo, int replicationDegree, byte[] chunkData, String chunkPath) {
        parentPeer.getPeerData().attachStoredObserver(this);

        this.handler = scheduledExecutor.schedule(
                () -> {
                    boolean success = saveChunk(fileID, chunkNo, replicationDegree, chunkData, chunkPath);
                    if (success) sendSTORED(request);
                },
                this.random.nextInt(MAX_DELAY + 1),
                TimeUnit.MILLISECONDS
        );

        try {
            this.handler.wait();
            parentPeer.getPeerData().detachStoredObserver(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean saveChunk(String fileID, int chunkNo, int replicationDegree, byte[] chunkData, String chunkPath) {
        SAVE_STATE ret;
        try {
            ret = parentPeer.getSystemManager().saveFile(
                    Integer.toString(chunkNo),
                    chunkPath,
                    chunkData
            );
        } catch (IOException e) {
            Log.logError("Couldn't save the chunk!");
            return false;
        }

        if (ret == SAVE_STATE.SUCCESS) {
            parentPeer.getDatabase().addChunk(
                    new ChunkInfo(fileID, chunkNo, replicationDegree, chunkData.length),
                    parentPeer.getID()
            );
        } else { // Don't send STORED if chunk already existed
            Log.logWarning("Chunk Backup: " + ret);
            return false;
        }

        return true;
    }

    private void sendSTORED(Message request) {
        Message msg = makeSTORED(request);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            Log.logError("Couldn't send message to multicast channel!");
        }
    }

    private void sendDelayedSTORED(Message request) {
        Message msg = makeSTORED(request);

        parentPeer.sendDelayedMessage(
                Channel.ChannelType.MC,
                msg,
                random.nextInt(MAX_DELAY + 1),
                TimeUnit.MILLISECONDS
        );
    }

    private Message makeSTORED(Message request) {
        String[] args = {
                parentPeer.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID(),
                Integer.toString(request.getChunkNo())
        };

        return new Message(Message.MessageType.STORED, args);
    }

    @Override
    public void update(Message msg) {
        if (this.handler == null)
            return;
        if (msg.getChunkNo() != request.getChunkNo() || (!msg.getFileID().equals(request.getFileID())))
            return;

        storedCount += 1;
        if (storedCount >= request.getReplicationDegree()) {
            // Cancel if chunk's perceived replication fulfills requirements
            this.handler.cancel(true);
        }
    }
}
