package protocols;

import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class Delete implements Runnable {

    private Peer parentPeer;
    private Message request;
    private Database database;

    public Delete(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        this.database = parentPeer.getDatabase();

        Log.logWarning("Starting delete!");
    }


    @Override
    public void run() {
        String fileID = request.getFileID();

        if(!database.hasChunks(fileID)){
            Log.logError("Chunks didn't exist! Aborting Delete!");
            return;
        }

        Set<Integer> chunks = database.getFileChunksKey(fileID);
        String path = parentPeer.getPath("chunks");

        for (Integer chunk : chunks) {
            try {
                Files.delete(Paths.get(path + "/" + fileID + "/" + chunk));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.delete(Paths.get(path + "/" + fileID));
        } catch (IOException e) {
            e.printStackTrace();
        }

        database.removeFileBackedUp(fileID);
    }
}