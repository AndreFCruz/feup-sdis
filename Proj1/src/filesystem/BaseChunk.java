package filesystem;

public class BaseChunk {
    private String fileID;
    private int chunkNo;
    private Integer replicationDegree;


    BaseChunk(String fileID, int chunkNo, Integer replicationDegree) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
    }

    protected void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
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
}
