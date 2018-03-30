package network;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.Database;
import protocols.*;
import protocols.initiators.helpers.RemovedChunkHelper;
import service.Peer;
import utils.Log;

import java.util.Random;
import java.util.concurrent.*;

public class Handler implements Runnable {
    private Peer parentPeer;
    private PeerData peerData;
    private BlockingQueue<Message> msgQueue;
    private ScheduledExecutorService executor;

    private Random random;

    public Handler(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.peerData = parentPeer.getPeerData();
        msgQueue = new LinkedBlockingQueue<>();
        executor = Executors.newScheduledThreadPool(5);

        this.random = new Random();
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

        Log.logWarning("R: " + msg.toString());

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
                if (parentPeer.getFlagRestored(msg.getFileID())) {
                    parentPeer.addChunkToRestore(new Chunk(msg.getFileID(), msg.getChunkNo(), msg.getBody()));
                } else {
                    Log.logWarning("Discard chunk, it's not for me");
                }
                break;
            case REMOVED:
                handleRemoved(msg);
                break;
            case DELETE:
                Delete delete = new Delete(parentPeer, msg);
                executor.execute(delete);
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

            executor.schedule(
                    new RemovedChunkHelper(parentPeer, chunkInfo, chunkData),
                    this.random.nextInt(ProtocolSettings.MAX_DELAY + 1),
                    TimeUnit.MILLISECONDS
                    );
        }
    }

    public void pushMessage(byte[] data, int length) {
        Message msgParsed = new Message(data, length); //create and parse the message
        msgQueue.add(msgParsed);
    }
}