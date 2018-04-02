package filesystem;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ChunkInfo extends BaseChunk implements Serializable {

    private int size;
    private Set<Integer> mirrors;

    public ChunkInfo(String fileID, int chunkNo, int replicationDegree, int size) {
        super(fileID, chunkNo, replicationDegree);

        this.mirrors = new HashSet<>();
        this.size = size;
    }

    /**
     * Removes the given peerID from the mirrors Set.
     * @param peerID
     * @return True if the peerID was a mirror, False otherwise
     */
    public boolean removeMirror(Integer peerID) {
        return mirrors.remove(peerID);
    }

    /**
     * Adds the given peerID to the mirrors Set.
     * @param peerID
     * @return True if addition was successful.
     */
    public boolean addMirror(Integer peerID) {
        return mirrors.add(peerID);
    }

    public int getNumMirrors() {
        return mirrors.size();
    }

    public int getSize() {
        return size;
    }

}
