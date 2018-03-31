package protocols.initiators;

import channels.Channel;
import filesystem.Database;
import filesystem.FileInfo;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteInitiator implements Runnable {
    private String version;
    private String path;
    private Peer parentPeer;

    public DeleteInitiator(String version, String path, Peer parentPeer) {
        this.version = version;
        this.path = path;
        this.parentPeer = parentPeer;

        Log.logWarning("Starting deleteInitiator!");
    }

    @Override
    public void run() {
        Database database = parentPeer.getDatabase();
        //Obtain info of the file from Database
        FileInfo fileInfo = database.getFileInfoByPath(path);
        if (fileInfo == null) {
            Log.logError("File didn't exist! Aborting Delete!");
            return;
        }

        Log.logError("Vou enviar");

        //Send Delete message to MC channel
        sendMessageToMC(fileInfo);
        Log.logError("Rip enviar");
        //Delete the file from fileSystem
        try {
            //TODO: Send delete messages 3/5 times with delay?
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Delete file from database
        database.removeRestorableFile(fileInfo);
        Log.logWarning("Finished deleteInitiator!");
    }

    private boolean sendMessageToMC(FileInfo fileInfo) {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileInfo.getFileID()
        };

        Message msg = new Message(Message.MessageType.DELETE, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
