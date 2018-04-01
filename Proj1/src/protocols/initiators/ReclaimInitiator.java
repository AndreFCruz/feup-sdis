package protocols.initiators;

import channels.Channel;
import filesystem.ChunkInfo;
import filesystem.SystemManager;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;

public class ReclaimInitiator implements Runnable {

    private Peer parentPeer;
    private SystemManager systemManager;
    private String version;

    public ReclaimInitiator(String version, Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.systemManager = parentPeer.getSystemManager();
        this.version = version;

        Log.logWarning("Starting reclaimInitiator!");
    }

    @Override
    public void run() {
        while (SystemManager.getAvailableMemory() < 0) {
            Log.logWarning("Available memory: " + SystemManager.getAvailableMemory());
            ChunkInfo chunkInfo = systemManager.getDatabase().getChunkForRemoval();
            byte[] chunkData = systemManager.loadChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());
            if (chunkData == null){ // Confirm chunk exists
                continue;
            }

            systemManager.deleteChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());

            try {
                sendREMOVED(chunkInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.logWarning("Available memory: " + SystemManager.getAvailableMemory());
        Log.logWarning("Finished reclaimInitiator!");
    }

    private void sendREMOVED(ChunkInfo chunkInfo) throws IOException {
        sendREMOVED(chunkInfo.getFileID(), chunkInfo.getChunkNo());
    }

    private void sendREMOVED(String fileID, int chunkNo) throws IOException {
        String args[] = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Message.MessageType.REMOVED, args);
        parentPeer.sendMessage(Channel.ChannelType.MC, msg);
    }

}
