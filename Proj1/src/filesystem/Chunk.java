package filesystem;

import utils.Log;

public class Chunk extends BaseChunk {

    private byte[] data;

    public Chunk(String fileID, int chunkNo, byte[] data) {
        super(fileID, chunkNo, null);
        this.data = data;
    }

    public Chunk(String fileID, int chunkNo, int replicationDegree, byte[] data) {
        this(fileID, chunkNo, data);
        setReplicationDegree(replicationDegree);

        Log.log("Created CHUNK " + fileID + " @" + chunkNo);
    }

    public Chunk(ChunkInfo chunkInfo, byte[] data) {
        this(chunkInfo.getFileID(), chunkInfo.getChunkNo(), chunkInfo.getReplicationDegree(), data);
    }


    public byte[] getData() {
        return data;
    }

    public int getSize() {
        return data.length;
    }

}