package protocols;

import channels.Channel;
import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;

public class Restore implements Runnable {

    private Peer parentPeer;
    private Message request;
    private Database database;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        this.database = parentPeer.getDatabase();

        Log.logWarning("Starting restore!");
    }

    @Override
    public void run() {
        //Ignore Chunks of own files
        if (request.getSenderID() == parentPeer.getID()) {
            Log.logWarning("Ignoring CHUNKs of own files");
            return;
        }

        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();

        //Access database to get the Chunk
        if (!database.hasChunk(fileID, chunkNo)) {
            Log.logError("Chunk not found locally: " + fileID + "/" + chunkNo);
            return;
        }

        byte[] chunkData = parentPeer.loadChunk(fileID, chunkNo);

        try {
            sendMessageToMDR(request, chunkData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.logWarning("Finished restore!");

    }

    private void sendMessageToMDR(Message msg, byte[] chunkData) throws IOException {
        String[] args = {
                msg.getVersion(),
                Integer.toString(parentPeer.getID()),
                msg.getFileID(),
                Integer.toString(msg.getChunkNo())
        };

        Message msgToSend = new Message(Message.MessageType.CHUNK, args, chunkData);

        parentPeer.sendMessage(Channel.ChannelType.MDR, msgToSend);
    }
}
