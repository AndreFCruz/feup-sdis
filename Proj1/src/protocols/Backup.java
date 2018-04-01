package protocols;

import channels.Channel;
import filesystem.ChunkInfo;
import filesystem.SystemManager.SAVE_STATE;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static filesystem.SystemManager.createFolder;
import static filesystem.SystemManager.saveFile;
import static protocols.ProtocolSettings.MAX_DELAY;

public class Backup implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Backup(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
    }


    @Override
    public void run() {
        int senderID = request.getSenderID();
        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();
        int replicationDegree = request.getReplicationDegree();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
//            Log.logWarning("Ignoring backup of own files");
            return;
        }

        byte[] chunkData = request.getBody();

        String chunkPath = parentPeer.getPath("chunks") + "/" + fileID;
        createFolder(parentPeer.getPath("chunks") + "/" + fileID);

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
        } else { // Don't send STORED if chunk already existed (?)
            Log.logWarning("Chunk Backup: " + ret);
        }

        Log.logWarning("Finished backup!");
    }

    private void sendSTORED(Message request) {
        String[] args = {
                request.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID(),
                Integer.toString(request.getChunkNo())
        };

        Message msg = new Message(Message.MessageType.STORED, args);

        Random random = new Random();
        parentPeer.sendDelayedMessage(Channel.ChannelType.MC, msg, random.nextInt(MAX_DELAY + 1), TimeUnit.MILLISECONDS);
    }
}
