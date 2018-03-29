package filesystem;

import java.util.ArrayList;

// TODO common subclass between Chunk and ChunkInfo, lots of replication
public class ChunkInfo {
    private String fileID; // chunkID -> fileID/chunkNo
    private int chunkNo;
    private int size;
    private int replicationDegree;
    private ArrayList<String> mirrors; //Not sure if it is the right place to store this

    public ChunkInfo(int chunkNo, int replicationDegree) {
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;

    }

    public ChunkInfo(String fileID, int chunkNo, int replicationDegree, int size) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.size = size;
    }


    public int getReplicationDegree() {
        return replicationDegree;
    }

    public ArrayList<String> getMirrors() {
        return mirrors;
    }

    public void removeMirror(String peerID) {
        mirrors.remove(peerID);
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
