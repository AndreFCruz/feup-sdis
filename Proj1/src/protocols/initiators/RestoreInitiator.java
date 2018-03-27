package protocols.initiators;

import filesystem.FileInfo;
import network.Message;
import service.Peer;
import utils.Utils;

import java.io.IOException;

public class RestoreInitiator implements Runnable{

    private FileInfo fileInfo;
    private String pathName;
    private Peer parentPeer;
    private String version;

    public RestoreInitiator(String version, String pathName, Peer parentPeer) {
        this.version = version;
        this.pathName = pathName;
        this.parentPeer = parentPeer;
        fileInfo = parentPeer.getFileFromDB(pathName); //handle if not exist??
    }

    @Override
    public void run() {
        try {
            if(fileInfo == null)
                return;

            //Send GETCHUNK to MC
            for(int i = 0; i < fileInfo.getNumChunks(); i++){
                sendMessageToMC(i);
            }

            //TODO:Handle Received Chunks

            //TODO:merge file and save

            //        saveFile("peras.png", "files",
//                fileMerge(
//                        loadChunks(
//                                systemManager.getChunksPath()+"/image1.png",
//                                18)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToMC(int chunkNo) throws IOException {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileInfo.getFileID(),
                Integer.toString(chunkNo),
        };

        Message msg = new Message(Utils.MessageType.GETCHUNK, args);

        parentPeer.sendMessage(0, msg);
    }

}
