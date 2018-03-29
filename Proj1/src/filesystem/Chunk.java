package filesystem;

import utils.Log;

public class Chunk {

    private String fileID;
    private int chunkNo;
    private int replicationDegree;
    private byte[] data;

    public Chunk(String fileID, int chunkNo, int replicationDegree, byte[] data) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.data = data;
        this.replicationDegree = replicationDegree;

        Log.logWarning("Created CHUNK " + fileID + " @" + chunkNo);
    }

    public Chunk(ChunkInfo chunkInfo, byte[] data) {
        this(chunkInfo.getFileID(), chunkInfo.getChunkNo(), chunkInfo.getReplicationDegree(), data);
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public byte[] getData() {
        return data;
    }

}