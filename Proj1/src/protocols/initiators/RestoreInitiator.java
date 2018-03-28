package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.FileInfo;
import network.Message;
import service.Peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static filesystem.SystemManager.fileMerge;
import static filesystem.SystemManager.saveFile;

public class RestoreInitiator implements Runnable {

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
            if (fileInfo == null)
                return;

            //Activate restore flag
            parentPeer.setRestoring(true, fileInfo.getFileID());

            //Send GETCHUNK to MC
            for (int i = 0; i < fileInfo.getNumChunks(); i++) {
                sendMessageToMC(i);
            }

            //TODO:Handle Received Chunks
            while (!parentPeer.hasRestoreFinished(pathName, fileInfo.getFileID())) {
                //Probably this will kill the cpu :')
                //And need to ask again if lose some chunks
            }
            System.out.println("Received all chunks");
            //TODO:merge file and save
            ConcurrentHashMap<String, Chunk> chunksRestored = parentPeer.getChunksRestored(fileInfo.getFileID());
            String pathToSave = parentPeer.getPath("restores");
            saveFile(fileInfo.getFileName(), pathToSave, fileMerge(convertHashMapToArray(chunksRestored)));

            //Delete restore flag
            parentPeer.setRestoring(false, fileInfo.getFileID());


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

        Message msg = new Message(Message.MessageType.GETCHUNK, args);

        parentPeer.sendMessage(Channel.ChannelType.MC, msg);
    }

    private ArrayList<Chunk> convertHashMapToArray(ConcurrentHashMap<String, Chunk> chunksRestored) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < fileInfo.getNumChunks(); i++) {
            chunks.add(chunksRestored.get(Integer.toString(i)));
        }
        return chunks;
    }

}
