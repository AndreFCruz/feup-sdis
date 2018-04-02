package filesystem;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo implements Serializable {
    private static final long serialVersionUID = 4L;

    private String fileID;
    private String pathName;
    private String fileName;
    private int numChunks;
    private int desiredReplicationDegree;
    private ChunkInfo[] chunks; //ChunkNo -> ChunkInfo


    public FileInfo(String filePath, String fileID, int replicationDegree, ChunkInfo[] chunkInfoArray) {
        this.fileID = fileID;
        Path path = Paths.get(filePath);
        this.fileName = path.getFileName().toString();
        this.pathName = path.toString();

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
