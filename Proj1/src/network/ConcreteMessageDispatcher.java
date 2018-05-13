package network;

import filesystem.ChunkData;
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

import static network.Message.MessageType.*;
import static protocols.ProtocolSettings.*;

public class ConcreteMessageDispatcher extends AbstractMessageDispatcher {
    private ScheduledExecutorService executor;

    private Map<String, Map<Integer, Future>> backUpHandlers;

    private Random random;

    public ConcreteMessageDispatcher(Peer parentPeer) {
        super(parentPeer);

        this.parentPeer = parentPeer;
        this.executor = Executors.newScheduledThreadPool(5);

        this.backUpHandlers = new HashMap<>();
        this.random = new Random();
    }

    private void handleGETCHUNK(Message msg) {
        Restore restore_enh = new Restore(parentPeer, msg);
        executor.execute(restore_enh);
    }

    private void handleDELETE(Message msg) {
        parentPeer.getDatabase().addToFilesToDelete(msg.getFileID());

        Delete delete = new Delete(parentPeer, msg);
        executor.execute(delete);
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
            return;
        }

        if (!isMessageCompatibleWithEnhancement(ENHANCEMENT_RESTORE, msg)) {
            peerData.addChunkToRestore(new ChunkData(msg.getFileID(), msg.getChunkNo(), msg.getBody()));
        }
    }

    private void handlePUTCHUNK(Message msg) {
        Database database = parentPeer.getDatabase();
        database.removeFromFilesToDelete(msg.getFileID());

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
            // If file is not a local file, Mirror/Backup ChunkData
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
            Log.log("Ignoring REMOVED of non-local ChunkData");
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

    @Override
    protected void setupMessageHandlers() {
        addMessageHandler(PUTCHUNK, this::handlePUTCHUNK);
        addMessageHandler(STORED, this::handleSTORED);
        addMessageHandler(GETCHUNK, this::handleGETCHUNK);
        addMessageHandler(ENH_GETCHUNK, this::handleGETCHUNK);
        addMessageHandler(CHUNK, this::handleCHUNK);
        addMessageHandler(REMOVED, this::handleREMOVED);
        addMessageHandler(DELETE, this::handleDELETE);
        addMessageHandler(DELETED, this::handleDELETED);
        addMessageHandler(UP, this::handleUP);
    }

}