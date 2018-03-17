package filesystem;

public class Chunk {
    private String fileID;
    private int chunkNo;
    private byte[] data;

    public Chunk(String fileID, int chunkNo, byte[] data) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.data = data;
    }

}
