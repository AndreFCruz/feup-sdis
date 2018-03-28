package filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FileInfo {

    private String fileID; // hashed through sha256
    private String pathname;
    private String fileName;
    private int numChunks;
    private int desiredReplicationDegree;
    private ConcurrentHashMap<String, ChunkInfo> chunks; //ChunkNO -> ChunkInfo


    public FileInfo(File file, String fileID, int replicationDegree, ConcurrentHashMap<String, ChunkInfo> chunksInfo) {
        this.fileID = fileID;
        this.fileName = file.getName();
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

    public String getFileName() {
        return fileName;
    }
}
