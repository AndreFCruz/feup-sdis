package protocols.initiators;

import filesystem.ChunkInfo;
import filesystem.Database;
import filesystem.FileInfo;
import filesystem.MemoryManager;
import service.Peer;
import utils.Log;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class RetrieveStateInitiator implements Runnable {
    private String version;
    private Peer parentPeer;
    private Database database;

    public RetrieveStateInitiator(String version, Peer parentPeer) {
        this.version = version;
        this.parentPeer = parentPeer;
        this.database = parentPeer.getDatabase();

        Log.logWarning("Starting retrieveStateInitiator!");
    }

    @Override
    public void run() {
        // Obtain info of the files from Database
        Collection<FileInfo> files = database.getFilesBackedUp();
        // Obtain info of the chunks from Database
        ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> chunks = database.getChunksBackedUp();

        // Save output string
        String out = "";

        // Loop to save the files
        out += "\nFiles:\n";
        for (FileInfo file : files) {
            out += "\nFile: " + file.getFileName() +
                    "\n Pathname: " + file.getPath() +
                    "\n FileID: " + file.getFileID() +
                    "\n Desired Replication: " + file.getDesiredReplicationDegree() +
                    "\n  Chunks:";
            ChunkInfo[] fileChunks = file.getChunks();
            for (ChunkInfo chunk : fileChunks) {
                out += "\n   ChunkID:" + chunk.getChunkNo();
            }
        }

        //Loop to save the chunks
        out += "\n\nChunks:\n";
        for (Map.Entry<String, ConcurrentMap<Integer, ChunkInfo>> outer : chunks.entrySet()) {
            out += "\nFile: " + outer.getKey();
            for (Map.Entry<Integer, ChunkInfo> inner : outer.getValue().entrySet()) {
                ChunkInfo chunk = inner.getValue();
                out += "\n Chunk: " +
                        "\n  ChunkID: " + chunk.getChunkNo() +
                        "\n  Size: " + chunk.getSize() / 1000 +
                        "\n  Perceived Replication: " + (chunk.getNumMirrors()+1);
            }
        }

        // Storage capacity
        MemoryManager mm = parentPeer.getSystemManager().getMemoryManager();
        out += "\n\nStorage: " +
                "\n Available memory: " + mm.getAvailableMemory() +
                "\n Used memory: " + mm.getUsedMemory();

        System.out.println(out); //TODO: Retrieve to TestApp
        Log.logWarning("Finished retrieveStateInitiator!");
    }

}
