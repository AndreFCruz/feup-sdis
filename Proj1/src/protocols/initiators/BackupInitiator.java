package protocols.initiators;

import network.Message;
import service.Peer;
import filesystem.FileManager;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class BackupInitiator implements Runnable {

    private byte[] fileData;
    private int replicationDegree;
    private File file;
    private String fileID;
    private Peer parentPeer;
    private String version;

    public BackupInitiator(String version, File file, int replicationDegree, Peer parentPeer) {
        this.version = version;
        this.file = file;
        this.replicationDegree = replicationDegree;
        this.parentPeer = parentPeer;
    }

    @Override
    public void run() {
        try {
            fileData = FileManager.loadFile(file);
            fileID = file.getName(); //temporary

            sendMessageToMDB();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToMDB() throws IOException {
        System.out.println(parentPeer);
        String [] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                "1",
                Integer.toString(replicationDegree)
        };

        Message msg = new Message(Utils.MessageType.PUTCHUNK, args);
        parentPeer.sendMessage(1, msg);
    }

    private void uploadFile() {


    }
}
