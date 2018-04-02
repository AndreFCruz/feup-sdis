package protocols;

import channels.Channel;
import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static protocols.ProtocolSettings.ENHANCEMENT_DELETE;
import static protocols.ProtocolSettings.isCompatibleWithEnhancement;

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

        if (!database.hasChunks(fileID)) {
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

        if (isCompatibleWithEnhancement(ENHANCEMENT_DELETE, request, parentPeer)){
            sendMessageToMC(request);
        }

        database.removeFileBackedUp(fileID);
        Log.logWarning("Finished delete!");
    }

    private boolean sendMessageToMC(Message request) {
        String[] args = {
                parentPeer.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID()
        };

        Message msg = new Message(Message.MessageType.DELETED, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}