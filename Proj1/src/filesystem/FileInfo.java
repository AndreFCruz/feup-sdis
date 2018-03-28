package filesystem;

import java.io.File;
import java.util.ArrayList;

public class FileInfo {

    private String fileID; // hashed through sha256
    private String pathname;
    private int numChunks;
    private int desiredReplicationDegree;
    private ArrayList<ChunkInfo> chunks;


    public FileInfo(String fileID, String pathname, int numChunks,
                    int desiredReplicationDegree, ArrayList<ChunkInfo> chunks) {

        this.fileID = fileID;
        this.pathname = pathname;
        this.numChunks = numChunks;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.chunks = chunks;
    }

    public FileInfo(File file, String fileID, int replicationDegree, ArrayList<ChunkInfo> chunksInfo) {
        this.fileID = fileID;
        this.pathname = file.getPath();
        this.numChunks = chunksInfo.size();
        this.desiredReplicationDegree = replicationDegree;
        this.chunks = chunksInfo;
    }

    public String getFileID() {
        return fileID;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public String getPathname() {
        return pathname;
    }
}
