package filesystem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileInfo {

    private String fileID; // hashed through sha256
    private String pathName;
    private String fileName;
    private int numChunks;
    private int desiredReplicationDegree;
    private ChunkInfo[] chunks; //ChunkNo -> ChunkInfo


    public FileInfo(File file, String fileID, int replicationDegree, ChunkInfo[] chunkInfoArray) {
        this.fileID = fileID;
        this.fileName = file.getName();
        this.pathName = file.getPath();
        this.numChunks = chunkInfoArray.length;
        this.desiredReplicationDegree = replicationDegree;
        this.chunks = chunkInfoArray;
    }

    public String getFileID() {
        return fileID;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public String getPathname() {
        return pathName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public ChunkInfo[] getChunks() {
        return chunks;
    }
}
