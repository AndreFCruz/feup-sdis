package protocols;

import network.Message;
import service.Peer;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static filesystem.SystemManager.loadFile;

public class Restore implements Runnable {

    private Peer parentPeer;
    private Message request;

    private String fileID;
    private int chunkNo;
    private String version;
    private int senderID;
    private byte[] chunkData;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting restore!");
    }


    @Override
    public void run() {

        version = request.getVersion();
        senderID = request.getSenderID();
        fileID = request.getFileID();
        chunkNo = request.getChunkNo();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
            System.out.println("Ignoring restore of own files"); //TODO: better message
            return;
        }

        //Access to database to get the Chunk
        String chunkID = fileID+"/"+chunkNo;
        if(parentPeer.hasChunkFromDB(chunkID)){
            try {
                //load chunk data
                chunkData = loadFile(new File(parentPeer.getPath("chunks")+chunkID));
                //send message to MDR
                sendMessageToMDR();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToMDR() throws IOException {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunkNo)
        };

        Message msg = new Message(Message.MessageType.CHUNK, args, chunkData);
        parentPeer.sendMessage(2, msg);
    }
}
