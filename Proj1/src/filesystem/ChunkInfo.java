package filesystem;

import java.util.ArrayList;

public class ChunkInfo {
    private String chunkID;
    private int size;
    private int replicationDegree;
    private ArrayList<String> mirrors; //Not sure if it is the right place to store this

    public ChunkInfo(int replicationDegree, ArrayList<String> mirrors) {
        this.replicationDegree = replicationDegree;
        this.mirrors = mirrors;
    }

    public ChunkInfo(int chunkNo, int replicationDegree) {
        this.chunkID = Integer.toString(chunkNo);
        this.replicationDegree = replicationDegree;

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

}
