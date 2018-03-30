package protocols;

import channels.Channel;
import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Restore implements Runnable, PeerData.MessageObserver {

    private Peer parentPeer;
    private Message request;
    private Database database;
    private Random random;
    private Future handler = null;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        this.database = parentPeer.getDatabase();
        this.random = new Random();

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
        sendMessageToMDR(request, chunkData);

        Log.logWarning("Finished restore!");
    }

    private void sendMessageToMDR(Message request, byte[] chunkData) {
        String[] args = {
                request.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID(),
                Integer.toString(request.getChunkNo())
        };

        Message msgToSend = new Message(Message.MessageType.CHUNK, args, chunkData);

        parentPeer.getPeerData().attachChunkObserver(this);
        this.handler = parentPeer.sendDelayedMessage(
                Channel.ChannelType.MDR,
                msgToSend,
                random.nextInt(ProtocolSettings.MAX_DELAY),
                TimeUnit.MILLISECONDS
        );

        try {
            this.handler.wait();
            parentPeer.getPeerData().detachChunkObserver(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void update(Message msg) {
        if (this.handler == null)
            return;
        if (msg.getFileID().equals(request.getFileID()) && msg.getChunkNo() == request.getChunkNo()) {
            this.handler.cancel(true);
            Log.log("Cancelled CHUNK message, to avoid flooding host");
        }
    }
}
