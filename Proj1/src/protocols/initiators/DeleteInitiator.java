package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import filesystem.SystemManager;
import network.Message;
import protocols.initiators.helpers.BackupChunkHelper;
import service.Peer;
import utils.Log;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

import static filesystem.SystemManager.fileSplit;

public class DeleteInitiator implements Runnable{
    private String version;
    private String pathName;
    private Peer parentPeer;

    public DeleteInitiator(String version, String pathName, Peer parentPeer) {
        this.version = version;
        this.pathName = pathName;
        this.parentPeer = parentPeer;
    }

    @Override
    public void run() {
        //Obtain info of the file from Database
        FileInfo fileInfo = parentPeer.getFileFromDB(pathName);
        if(fileInfo == null){
            Log.logError("File didn't exist! Aborting Delete!");
            return;
        }
        //Send Delete message to MC channel
        sendMessageToMC(fileInfo);

        //Delete the file from fileSystem
        try {
            //TODO: Send delete messages 3/5 times with delay?
            Files.delete(Paths.get(pathName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Delete file from database
        parentPeer.deleteFileToDB(pathName);

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
