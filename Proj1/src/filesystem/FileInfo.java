package filesystem;

import java.io.File;
import java.util.HashMap;

public class FileInfo {

    private String fileID; // hashed through sha256
    private String pathName;
    private String fileName;
    private int numChunks;
    private int desiredReplicationDegree;
    private HashMap<String, ChunkInfo> chunks; //ChunkNo -> ChunkInfo


    public FileInfo(File file, String fileID, int replicationDegree, HashMap<String, ChunkInfo> chunksInfo) {
        this.fileID = fileID;
        this.fileName = file.getName();
        this.pathName = file.getPath();
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
        return pathName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public HashMap<String, ChunkInfo> getChunks() {
        return chunks;
    }
}
