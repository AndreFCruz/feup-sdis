package protocols;

import channels.Channel;
import filesystem.ChunkInfo;
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

    private byte[] chunkData;
    private int replicationDegree;
    private String fileID;
    private int chunkNo;
    private String version;
    private int senderID;

    public Backup(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        Log.logWarning("Starting backup!");
    }


    @Override
    public void run() {

        version = request.getVersion();
        senderID = request.getSenderID();
        fileID = request.getFileID();
        chunkNo = request.getChunkNo();
        replicationDegree = request.getReplicationDegree();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
            Log.logWarning("Ignoring backup of own files");
            return;
        }

        chunkData = request.getBody();

        String chunkPathname = parentPeer.getPath("chunks") + "/" + fileID;

        createFolder(parentPeer.getPath("chunks") + "/" + fileID);

        boolean success = false;
        try {
            success = saveFile(Integer.toString(chunkNo), chunkPathname, chunkData);
            //save to database
            parentPeer.addChunkToDB(new ChunkInfo(fileID, chunkNo, replicationDegree, chunkData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (! success) {
            Log.logWarning("Did not backup Chunk, and did not send STORED: MAX MEMORY REACHED");
        } else {
            sendSTORED();
        }

        Log.logWarning("Finished backup!");
    }

    private void sendSTORED() {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Message.MessageType.STORED, args);

        Random random = new Random();
        parentPeer.sendDelayedMessage(Channel.ChannelType.MC, msg, random.nextInt(MAX_DELAY + 1), TimeUnit.MILLISECONDS);
    }
}
