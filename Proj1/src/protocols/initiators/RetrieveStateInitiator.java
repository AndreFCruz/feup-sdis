package protocols.initiators;

import filesystem.ChunkInfo;
import filesystem.Database;
import filesystem.FileInfo;
import service.Peer;
import utils.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class RetrieveStateInitiator implements Runnable {
    private Peer parentPeer;
    private Database database;

    public RetrieveStateInitiator(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.database = parentPeer.getDatabase();
        Log.logWarning("Starting retriveStateInitiator!");
    }

    @Override
    public void run() {
        //Obtain info of the files from Database
        ArrayList<FileInfo> files = new ArrayList<>(database.getFilesBackedUp().values());
        //Obtain info of the chunks from Database
        ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> chunks = database.getChunksBackedUp();

        //Save output string
        String out = "";

        //Loop to save the files
        out += "\nFiles:\n";
        for (FileInfo file : files) {
            out += "\nFile: " + file.getFileName() +
                    "\n Pathname: " + file.getPathname() +
                    "\n FileID: " + file.getFileID() +
                    "\n Desired Replication: " + file.getDesiredReplicationDegree() +
                    "\n  Chunks:";
            ArrayList<ChunkInfo> fileChunks = new ArrayList<>(file.getChunks().values());
            for (ChunkInfo chunk : fileChunks) {
                out += "\n   ChunkID:" + chunk.getChunkNo() +
                        "\n   Perceived Replication:" + chunk.getReplicationDegree(); //TODO:Update this value
            }
        }

        //Loop to save the chunks
        out += "\n\nChunks:\n"; //TODO: Correct values
        for (Map.Entry<String, ConcurrentMap<Integer, ChunkInfo>> outer : chunks.entrySet()) {
            out += "\nFile: " + outer.getKey();
            for (Map.Entry<Integer, ChunkInfo> inner : outer.getValue().entrySet()) {
                ChunkInfo chunk = inner.getValue();
                out += "\n Chunk: " +
                        "\n  ChunkID: " + chunk.getChunkNo() +
                        "\n  Size: " + chunk.getSize() / 1000 +
                        "\n  Perceived Replication: " + chunk.getReplicationDegree();
            }
        }


        //Storage capacity
        out += "\n\nStorage: " +
                "\n Available memory: " + parentPeer.getSystemManager().getAvailableMemory() +
                "\n Used memory: " + parentPeer.getSystemManager().getUsedMemory();

        System.out.println(out); //TODO: Retrieve to TestApp
        Log.logWarning("Finished retrieveStateInitiator!");
    }

}
