package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.FileInfo;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import static filesystem.SystemManager.fileMerge;
import static filesystem.SystemManager.saveFile;

public class RestoreInitiator implements Runnable {

    private FileInfo fileInfo;
    private String filePath;
    private String version;

    private Peer parentPeer;

    public RestoreInitiator(String version, String filePath, Peer parentPeer) {
        this.version = version;
        this.filePath = filePath;
        this.parentPeer = parentPeer;
        fileInfo = parentPeer.getDatabase().getFileInfoByPath(filePath);

        Log.logWarning("Starting restoreInitiator!");
    }

    @Override
    public void run() {
        if (fileInfo == null) {
            Log.logError("File not found for RESTORE");
            return;
        }

        // Activate restore flag
        parentPeer.setRestoring(true, fileInfo.getFileID());

        //Log.logWarning("Sending GETCHUNK messages");
        // Send GETCHUNK to MC
        for (int i = 0; i < fileInfo.getNumChunks(); i++) {
            sendMessageToMC(i);
        }

        //Log.logWarning("Waiting for restored chunks");
        while (!parentPeer.hasRestoreFinished(filePath, fileInfo.getFileID())) {
            //Probably this will kill the cpu :')
            //And need to ask again if lose some chunks
            // TODO sleep ?
        }
//        Log.logWarning("Received all chunks");
        ConcurrentMap<Integer, Chunk> chunksRestored = parentPeer.getChunksRestored(fileInfo.getFileID());
        String pathToSave = parentPeer.getPath("restores");

        try {
            saveFile(fileInfo.getFileName(), pathToSave, fileMerge(new ArrayList<>(chunksRestored.values())));
        } catch (IOException e) {
            e.printStackTrace();
            Log.logError("Failed saving file at " + fileInfo.getPathname());
        }

        // File no longer restoring
        parentPeer.setRestoring(false, fileInfo.getFileID());
        Log.logWarning("Finished restoreInitiator!");
    }

    private boolean sendMessageToMC(int chunkNo) {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileInfo.getFileID(),
                Integer.toString(chunkNo),
        };

        Message msg = new Message(Message.MessageType.GETCHUNK, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
