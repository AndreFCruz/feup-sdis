package protocols;

import filesystem.FileManager;
import network.Message;
import service.Peer;
import utils.Utils;

import java.io.IOException;

public class Backup implements Runnable{

    private Peer parentPeer;
    private Message request;

    private byte[] chunkData;
    private int replicationDegree;
    private String fileID;
    private int chunkNo;
    private String version;
    private int senderID;

    public Backup(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting backup!");
    }


    @Override
    public void run() {

        version = request.getVersion();
        senderID = request.getSenderID();
        fileID = request.getFileID();
        chunkNo = request.getChunkNo();
        replicationDegree = request.getReplicationDegree();

        if(senderID == parentPeer.getID()) { // a peer never stores the chunks of it own files
            System.out.println("Ignore backup");
            return;
        }

        chunkData = request.getBody();

        try {
                sendMessageToMC();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToMC() throws IOException {
        System.out.println(parentPeer);

        String [] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Utils.MessageType.STORED, args);
        parentPeer.sendMessage(0, msg);
    }
}
