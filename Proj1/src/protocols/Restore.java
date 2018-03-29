package protocols;

import channels.Channel;
import network.Message;
import service.Peer;
import utils.Log;

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

        Log.logWarning("Starting restore!");
    }


    @Override
    public void run() {

        version = request.getVersion();
        senderID = request.getSenderID();
        fileID = request.getFileID();
        chunkNo = request.getChunkNo();

        if (senderID == parentPeer.getID()) { // a peer never stores the chunks of its own files
            Log.logWarning("Ignoring restore of own files"); //TODO: better message
            return;
        }

        //Access to database to get the Chunk
        if (parentPeer.hasChunkFromDB(fileID, chunkNo)) {
            String chunkPath = parentPeer.getPath("chunks") + fileID + "/" + chunkNo;
            try {
                //load chunk data
                chunkData = loadFile(new File(chunkPath));
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
        parentPeer.sendMessage(Channel.ChannelType.MDR, msg);
    }
}
