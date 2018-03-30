package protocols;

import filesystem.Chunk;
import network.Message;
import utils.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class PeerData {
    interface MessageObserver {
        void update(Message msg);
    }

    /**
     * Contains number of confirmed STORE messages received,
     * for Chunks of local files (from BackupInitiator).
     * Maps (fileID -> (ChunkNum -> NumStoresReceived))
     */
    private ConcurrentMap<String, AtomicIntegerArray> chunkReplication;

    /**
     * Contains the in-memory chunks restored.
     * Maps (fileID -> (ChunkNum -> Chunk))
     */
    private ConcurrentMap<String, ConcurrentSkipListMap<Integer, Chunk>> chunksRestored;

    /**
     * Collection of Observers of CHUNK messages.
     * Used for Restore protocol.
     */
    private ConcurrentSkipListSet<MessageObserver> chunkObservers;

    public PeerData() {
        chunkReplication = new ConcurrentHashMap<>();
        chunksRestored = new ConcurrentHashMap<>();
        chunkObservers = new ConcurrentSkipListSet<>();
    }

    public void attachChunkObserver(MessageObserver observer) {
        this.chunkObservers.add(observer);
    }

    public void detachChunkObserver(MessageObserver observer) {
        this.chunkObservers.remove(observer);
    }

    public void notifyChunkObservers(Message msg) {
        for (MessageObserver observer : chunkObservers)
            observer.update(msg);
    }

    public void setFlagRestored(boolean flag, String fileID) {
        if (flag) {
            chunksRestored.putIfAbsent(fileID, new ConcurrentSkipListMap<>());
        } else {
            chunksRestored.remove(fileID);
        }
    }

    public boolean getFlagRestored(String fileID) {
        return chunksRestored.containsKey(fileID);
    }

    public void addChunkToRestore(Chunk chunk) {
        Chunk ret = chunksRestored.get(chunk.getFileID()).putIfAbsent(chunk.getChunkNo(), chunk);

        if (ret != null) {
            Log.logWarning("Chunk already exists");
        } else {
            Log.logWarning("Adding chunk to merge");
        }
    }

    public Integer getChunksRestoredSize(String fileID) {
        return chunksRestored.get(fileID).size();
    }

    public ConcurrentMap<Integer, Chunk> getChunksRestored(String fileID) {
        return chunksRestored.get(fileID);
    }

    public void resetChunkReplication(String fileID) {
        chunkReplication.remove(fileID);
    }

    public void startChunkReplication(String fileID, int numChunks) {
        Log.log("Starting rep. log at key " + fileID);
        chunkReplication.putIfAbsent(fileID, new AtomicIntegerArray(numChunks));
    }

    public Integer addChunkReplication(String fileID, int chunkNo) {
        if (!chunkReplication.containsKey(fileID)) {
            Log.logWarning("addChunkReplication: key not found: " + fileID);
            return null;
        }

        int replication = chunkReplication.get(fileID).addAndGet(chunkNo, 1);
        Log.logWarning("Incrementing replication of " + fileID + "/" + chunkNo + " to " + replication);
        return replication;
    }

    public int getChunkReplication(String fileID, int chunkNo) {
        return chunkReplication.get(fileID).get(chunkNo);
    }

    public AtomicIntegerArray getChunkReplication(String fileID) {
        return chunkReplication.get(fileID);
    }
}
