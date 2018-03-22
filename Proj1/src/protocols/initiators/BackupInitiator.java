package protocols.initiators;

import service.Peer;
import filesystem.FileManager;

import java.io.File;
import java.io.FileNotFoundException;


public class BackupInitiator implements Runnable {

    private byte[] fileData;
    private int replicationDegree;
    private File file;
    private String fileID;
    private Peer parentPeer;

    public BackupInitiator(File file, int replicationDegree, Peer parentPeer) {
        this.file = file;
        this.replicationDegree = replicationDegree;
        this.parentPeer = parentPeer;
    }

    @Override
    public void run() {
        try {
            fileData = FileManager.loadFile(file);
            fileID = file.getName(); //temporary
            System.out.println(parentPeer);
            parentPeer.sendMessage(1, "vou dar upload");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void uploadFile() {


    }
}
