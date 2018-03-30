package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.SystemManager;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;

public class ReclaimInitiator implements Runnable {

    private Peer parentPeer;
    private SystemManager systemManager;

    public ReclaimInitiator(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.systemManager = parentPeer.getSystemManager();

        Log.logWarning("Starting reclaimInitiator!");
    }

    @Override
    public void run() {
        while (systemManager.getAvailableMemory() < 0) {
            ChunkInfo chunkInfo = systemManager.getDatabase().getChunkForRemoval();
            byte[] chunkData = systemManager.loadChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());
            if (chunkData == null)
                return;

            systemManager.deleteChunk(chunkInfo.getFileID(), chunkInfo.getChunkNo());

            try {
                sendREMOVED(chunkInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.logWarning("Finished reclaimInitiator!");
    }

    private void sendREMOVED(ChunkInfo chunkInfo) throws IOException {
        sendREMOVED(chunkInfo.getFileID(), chunkInfo.getChunkNo());
    }

    private void sendREMOVED(String fileID, int chunkNo) throws IOException {
        String args[] = {
                Peer.PROTOCOL_VERSION,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Message.MessageType.REMOVED, args);
        parentPeer.sendMessage(Channel.ChannelType.MC, msg);
    }

}
