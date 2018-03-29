package network;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.Database;
import protocols.Backup;
import protocols.PeerData;
import protocols.Restore;
import protocols.initiators.helpers.RemovedChunkHelper;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Handler implements Runnable {
    private Peer parentPeer;
    private PeerData peerData;
    private BlockingQueue<Message> msgQueue;
    private ExecutorService executor;

    public Handler(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.peerData = parentPeer.getPeerData();
        msgQueue = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool(3);
    }


    @Override
    public void run() {
        Message msg;

        while (true) {
            try {
                msg = msgQueue.take();
                dispatchMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchMessage(Message msg) {
        if (msg == null) {
            Log.logError("Null Message Received");
            return;
        }

        Log.logWarning("R: " + msg.getHeaderAsString() + "|");
        switch (msg.getType()) {
            case PUTCHUNK:
                Backup backup = new Backup(parentPeer, msg);
                executor.execute(backup);
                break;
            case STORED:
                peerData.addChunkReplication(msg.getFileID(), msg.getChunkNo());
                break;
            case GETCHUNK:
                Restore restore = new Restore(parentPeer, msg);
                executor.execute(restore);
                break;
            case CHUNK:
                Log.logWarning("Chunk received");
                if (parentPeer.getFlagRestored(msg.getFileID())) {
                    parentPeer.addChunkToRestore(new Chunk(msg.getFileID(), msg.getChunkNo(), -1, msg.getBody()));
                } else {
                    Log.logWarning("Discard chunk, it's not for me");
                }
                break;
            case REMOVED:
                Log.logWarning("Received REMOVED");
                handleRemoved(msg);
                break;
            default:
                return;

        }

    }

    private void handleRemoved(Message msg) {
        Database database = parentPeer.getDatabase();
        String fileID = msg.getFileID();
        int chunkNo = msg.getChunkNo();

        database.removeChunkMirror(fileID, chunkNo, msg.getSenderID());

        ChunkInfo chunkInfo = database.getChunkInfo(fileID, chunkNo);

        int perceivedReplication = database.getChunkPerceivedReplication(fileID, chunkNo);
        int desiredReplication = chunkInfo.getReplicationDegree();
        if (perceivedReplication < desiredReplication) {
            byte[] chunkData = parentPeer.loadChunk(fileID, chunkNo);
            executor.execute(() -> {
                new Thread(new RemovedChunkHelper(parentPeer, chunkInfo, chunkData)).start();
            });
        }
    }

    public void pushMessage(byte[] data, int length) throws IOException {
        Message msgParsed = new Message(data, length); //create and parse the message
        msgQueue.add(msgParsed);
    }
}