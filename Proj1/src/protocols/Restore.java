package protocols;

import channels.Channel;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static filesystem.SystemManager.loadFile;

public class Restore implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        Log.logWarning("Starting restore!");
    }


    @Override
    public void run() {
        if (request.getSenderID() == parentPeer.getID()) { // ignore Chunks of own files
            Log.logWarning("Ignoring CHUNKs of own files");
            return;
        }

        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();
        //Access database to get the Chunk

        byte[] chunkData = parentPeer.loadChunk(fileID, chunkNo);
        if (chunkData == null) {
            Log.logError("Chunk not found locally: " + fileID + "/" + chunkNo);
        }

        try {
            sendMessageToMDR(request, chunkData);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
