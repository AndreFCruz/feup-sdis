package protocols;

import filesystem.Chunk;
import utils.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class PeerData {

    /**
     * Contains number of confirmed STORE messages received.
     * Maps (fileID -> (ChunkNum -> NumStoresReceived))
     */
    private ConcurrentMap<String, AtomicIntegerArray> chunkReplication;

    // TODO _inner_ String to Int, and _inner_ ConcurrentHashMap to ConcurrentSkipListMap
    /**
     * Contains the in-memory chunks restored.
     * Maps (fileID -> (ChunkNum -> Chunk))
     */
    private ConcurrentMap<String, ConcurrentHashMap<String, Chunk>> chunksRestored;
//    private ConcurrentMap<String, ConcurrentSkipListMap<Integer, Chunk>> chunksRestored;

    public PeerData() {
        chunkReplication = new ConcurrentHashMap<>();
        chunksRestored = new ConcurrentHashMap<>();

    }

    public void setFlagRestored(boolean flag, String fileID) {
        if (flag) {
            chunksRestored.put(fileID, new ConcurrentHashMap<>());
        } else {
            chunksRestored.remove(fileID);
        }
    }

    public boolean getFlagRestored(String fileID) {
        return chunksRestored.containsKey(fileID);
    }

    public void addChunksRestored(Chunk chunk) {
        if (chunksRestored.get(chunk.getFileID()).containsKey(Integer.toString(chunk.getChunkNo()))) {
            Log.logWarning("Chunk already exist");
        } else {
            Log.logWarning("Adding chunk to merge");
            chunksRestored.get(chunk.getFileID()).put(Integer.toString(chunk.getChunkNo()), chunk);
        }

    }

    public Integer getChunksRestoredSize(String fileID) {
        return chunksRestored.get(fileID).size();
    }

    public ConcurrentHashMap<String, Chunk> getChunksRestored(String fileID) {
        return chunksRestored.get(fileID);
    }

    public void resetChunkReplication(String fileID) {
        chunkReplication.remove(fileID);
    }

    public void startChunkReplication(String fileID, int numChunks) {
        chunkReplication.putIfAbsent(fileID, new AtomicIntegerArray(numChunks));
    }

    public Integer addChunkReplication(String fileID, int chunkNo) {
        if (! chunkReplication.containsKey(fileID))
            return null;
        Log.logWarning("Incrementeing replication of " + fileID + " at " + chunkNo);
        return chunkReplication.get(fileID).addAndGet(chunkNo, 1);
    }

    public int getChunkReplication(String fileID, int chunkNo) {
        return chunkReplication.get(fileID).get(chunkNo);
    }

    public AtomicIntegerArray getChunkReplication(String fileID) {
        return chunkReplication.get(fileID);
    }
}
