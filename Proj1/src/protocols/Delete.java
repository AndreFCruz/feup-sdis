package protocols;

import channels.Channel;
import filesystem.ChunkInfo;
import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static protocols.ProtocolSettings.ENHANCEMENT_DELETE;
import static protocols.ProtocolSettings.isCompatibleWithEnhancement;

public class Delete implements Runnable {

    private Peer parentPeer;
    private Message request;
    private Database database;

    public Delete(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        this.database = parentPeer.getDatabase();

        Log.logWarning("Starting delete!");
    }


    @Override
    public void run() {
        String fileID = request.getFileID();

        if (!database.hasChunks(fileID)) {
            Log.logError("Chunks didn't exist! Aborting Delete!");
            return;
        }

        Map<Integer, ChunkInfo> chunkMap = database.removeChunksBackedUpByFileID(fileID);
        Collection<ChunkInfo> chunks = chunkMap.values();
        for (ChunkInfo chunk : chunks) {
            parentPeer.getSystemManager().deleteChunk(chunk.getFileID(), chunk.getChunkNo());
        }

        if (isCompatibleWithEnhancement(ENHANCEMENT_DELETE, request, parentPeer)) {
            sendMessageToMC(request);
        }

        Log.logWarning("Finished delete!");
    }

    private void sendMessageToMC(Message request) {
        Message msg = makeDELETED(request);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            Log.logError("Couldn't send message to multicast channel!");
        }
    }

    private Message makeDELETED(Message request) {
        String[] args = {
                parentPeer.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID()
        };

        return new Message(Message.MessageType.DELETED, args);
    }

}