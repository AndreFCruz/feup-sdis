package network;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.Database;
import protocols.*;
import protocols.initiators.helpers.DeleteEnhHelper;
import protocols.initiators.helpers.RemovedChunkHelper;
import service.Peer;
import utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static protocols.ProtocolSettings.*;

public class ConcreteMessageHandler extends MessageHandler {
    private Peer parentPeer;
    private ScheduledExecutorService executor;
    private Map<String, Map<Integer, Future>> backUpHandlers;

    private Random random;

    public ConcreteMessageHandler(Peer parentPeer) {
        super();

        this.parentPeer = parentPeer;
        this.executor = Executors.newScheduledThreadPool(5);

        this.backUpHandlers = new HashMap<>();
        this.random = new Random();
    }

    @Override
    protected void dispatchMessage(Message msg) {
        //Ignoring invalid messages
        if (msg == null) {
            Log.logError("Null message received!");
            return;
        }

        // Ignoring own messages
        if (msg.getSenderID() == parentPeer.getID())
            return;

        //Print received messages
        Log.logWarning("R: " + msg.toString());

        switch (msg.getType()) {
            case PUTCHUNK:
                handlePUTCHUNK(msg);
                break;
            case STORED:
                handleSTORED(msg);
                break;
            case GETCHUNK:
                Restore restore = new Restore(parentPeer, msg);
                executor.execute(restore);
                break;
            case ENH_GETCHUNK:
                Restore restore_enh = new Restore(parentPeer, msg);
                executor.execute(restore_enh);
                break;
            case CHUNK:
                handleCHUNK(msg);
                break;
            case REMOVED:
                handleREMOVED(msg);
                break;
            case DELETE:
                Delete delete = new Delete(parentPeer, msg);
                executor.execute(delete);
                break;
            case DELETED:
                handleDELETED(msg);
                break;
            case UP:
                handleUP(msg);
                break;
            default:
                return;
        }
    }

    private void handleUP(Message msg) {
        if (isCompatibleWithEnhancement(ENHANCEMENT_DELETE, msg, parentPeer)) {
            executor.execute(new DeleteEnhHelper(msg, parentPeer));
        }
    }

    private void handleDELETED(Message msg) {
        Database database = parentPeer.getDatabase();

        if (isCompatibleWithEnhancement(ENHANCEMENT_DELETE, msg, parentPeer)) {
            database.deleteFileMirror(msg.getFileID(), msg.getSenderID());
        }
    }

    private void handleCHUNK(Message msg) {
        PeerData peerData = parentPeer.getPeerData();

        // Notify RESTORE observers of new CHUNK message
        peerData.notifyChunkObservers(msg);

        if (!peerData.getFlagRestored(msg.getFileID())) { // Restoring File
            Log.log("Discarded Chunk");
            return;
        }

        if (!isMessageCompatibleWithEnhancement(ENHANCEMENT_RESTORE, msg)) {
            peerData.addChunkToRestore(new Chunk(msg.getFileID(), msg.getChunkNo(), msg.getBody()));
        }
    }

    private void handlePUTCHUNK(Message msg) {
        Database database = parentPeer.getDatabase();

        if (database.hasChunk(msg.getFileID(), msg.getChunkNo())) {
            // If chunk is backed up by parentPeer, notify
            Map<Integer, Future> fileBackUpHandlers = backUpHandlers.get(msg.getFileID());
            if (fileBackUpHandlers == null) return;

            final Future handler = fileBackUpHandlers.remove(msg.getChunkNo());
            if (handler == null) return;
            handler.cancel(true);
            Log.log("Stopping chunk back up, due to received PUTCHUNK");
        }
        else if (! database.hasBackedUpFileById(msg.getFileID())) {
            // If file is not a local file, Mirror/Backup Chunk
            Backup backup = new Backup(parentPeer, msg);
            executor.execute(backup);
        } else {
            Log.log("Ignoring PUTCHUNK of own file");
        }
    }

    private void handleSTORED(Message msg) {
        // Notify BACKUP observers of new STORED message
        parentPeer.getPeerData().notifyStoredObservers(msg);

        Database database = parentPeer.getDatabase();
        if (database.hasChunk(msg.getFileID(), msg.getChunkNo())) {
            database.addChunkMirror(msg.getFileID(), msg.getChunkNo(), msg.getSenderID());
        } else if (database.hasBackedUpFileById(msg.getFileID())) {
            parentPeer.getPeerData().addChunkReplication(msg.getFileID(), msg.getChunkNo());
            database.addFileMirror(msg.getFileID(), msg.getSenderID());
        }
    }

    private void handleREMOVED(Message msg) {
        Database database = parentPeer.getDatabase();
        String fileID = msg.getFileID();
        int chunkNo = msg.getChunkNo();

        if (database.removeChunkMirror(fileID, chunkNo, msg.getSenderID()) == null) {
            Log.log("Ignoring REMOVED of non-local Chunk");
            return;
        }

        ChunkInfo chunkInfo = database.getChunkInfo(fileID, chunkNo);

        int perceivedReplication = database.getChunkPerceivedReplication(fileID, chunkNo);
        int desiredReplication = chunkInfo.getReplicationDegree();

        if (perceivedReplication < desiredReplication) {
            byte[] chunkData = parentPeer.loadChunk(fileID, chunkNo);

            Future handler = executor.schedule(
                    new RemovedChunkHelper(parentPeer, chunkInfo, chunkData),
                    this.random.nextInt(ProtocolSettings.MAX_DELAY + 1),
                    TimeUnit.MILLISECONDS
            );

            backUpHandlers.putIfAbsent(msg.getFileID(), new HashMap<>());
            backUpHandlers.get(msg.getFileID()).put(msg.getChunkNo(), handler);
        }
    }
}