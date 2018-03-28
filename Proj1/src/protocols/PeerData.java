package protocols;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class PeerData {

    /**
     * Contains number of confirmed STORE messages received.
     * Maps (fileID -> (ChunkNum -> NumStoresReceived))
     */
    private ConcurrentMap<String, AtomicIntegerArray> chunkReplication;

    public PeerData() {
        chunkReplication = new ConcurrentHashMap<>();

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
        System.out.println("Incrementeing replication of " + fileID + " at " + chunkNo);
        return chunkReplication.get(fileID).addAndGet(chunkNo, 1);
    }

    public int getChunkReplication(String fileID, int chunkNo) {
        return chunkReplication.get(fileID).get(chunkNo);
    }
}
