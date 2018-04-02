package protocols.initiators;

import channels.Channel;
import filesystem.ChunkInfo;
import filesystem.MemoryManager;
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

        Log.log("Starting reclaimInitiator!");
    }

    @Override
    public void run() {
        MemoryManager memoryManager = systemManager.getMemoryManager();
        while (memoryManager.getAvailableMemory() < 0) {
            Log.log("Available memory: " + memoryManager.getAvailableMemory());
            ChunkInfo chunkInfo = systemManager.getDatabase().getChunkForRemoval();

            byte[] chunkData = systemManager.loadChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());
            if (chunkData == null) { // Confirm chunk exists
                Log.logWarning("Chunk selected for reclaim doesn't exist");
                continue;
            }

            systemManager.deleteChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());
            sendREMOVED(chunkInfo);
        }

        Log.log("Available memory: " + memoryManager.getAvailableMemory());
        Log.log("Finished reclaimInitiator!");
    }

    private void sendREMOVED(ChunkInfo chunkInfo) {
        sendREMOVED(chunkInfo.getFileID(), chunkInfo.getChunkNo());
    }

    private void sendREMOVED(String fileID, int chunkNo) {
        String args[] = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Message.MessageType.REMOVED, args);
        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            Log.logError("Couldn't send message to multicast channel!");
        }
    }

}
