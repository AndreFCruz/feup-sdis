package filesystem;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ChunkInfo implements Serializable {

    private String fileID;
    private int chunkNo;
    private int size;
    private Integer replicationDegree;
    private Set<Integer> mirrors;

    public ChunkInfo(int chunkNo, int replicationDegree) {
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.mirrors = new HashSet<>();
    }

    public ChunkInfo(String fileID, int chunkNo, int replicationDegree, int size) {
        this(chunkNo, replicationDegree);

        this.fileID = fileID;
        this.size = size;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * Removes the given peerID from the mirrors Set
     *
     * @param peerID
     * @return True if the peerID was a mirror, False otherwise
     */
    public boolean removeMirror(Integer peerID) {
        return mirrors.remove(peerID);
    }

    public boolean addMirror(Integer peerID) {
        return mirrors.add(peerID);
    }

    public int getNumMirrors() {
        return mirrors.size();
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getSize() {
        return size;
    }

}
