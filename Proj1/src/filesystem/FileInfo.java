package filesystem;

import utils.Log;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo implements Serializable {

    private String fileID; // hashed through sha256
    private String pathName;
    private String fileName;
    private int numChunks;
    private int desiredReplicationDegree;
    private ChunkInfo[] chunks; //ChunkNo -> ChunkInfo


    public FileInfo(String pathName, String fileID, int replicationDegree, ChunkInfo[] chunkInfoArray) {
        this.fileID = fileID;
        Path filepath = Paths.get(pathName);
        this.fileName = filepath.getFileName().toString();
        this.pathName = filepath.toString();
        Log.log(fileName);
        Log.log(pathName);

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

    public String getPath() {
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
