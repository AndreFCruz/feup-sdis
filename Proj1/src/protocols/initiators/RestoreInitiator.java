package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.FileInfo;
import network.Message;
import protocols.PeerData;
import service.Peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static filesystem.SystemManager.fileMerge;
import static filesystem.SystemManager.saveFile;

public class RestoreInitiator implements Runnable {

    private FileInfo fileInfo;
    private String pathName;
    private String version;

    private Peer parentPeer;

    public RestoreInitiator(String version, String pathName, Peer parentPeer) {
        this.version = version;
        this.pathName = pathName;
        this.parentPeer = parentPeer;
        fileInfo = parentPeer.getFileFromDB(pathName); // TODO handle if doesn't exist
    }

    @Override
    public void run() {
        if (fileInfo == null)
            return;

        // Activate restore flag
        parentPeer.setRestoring(true, fileInfo.getFileID());

        // Send GETCHUNK to MC
        for (int i = 0; i < fileInfo.getNumChunks(); i++) {
            sendMessageToMC(i);
        }

        //TODO: handle Received Chunks
        while (!parentPeer.hasRestoreFinished(pathName, fileInfo.getFileID())) {
            //Probably this will kill the cpu :')
            //And need to ask again if lose some chunks
            // TODO sleep ?
        }
        System.out.println("Received all chunks");
        //TODO: merge file and save
        ConcurrentHashMap<String, Chunk> chunksRestored = parentPeer.getChunksRestored(fileInfo.getFileID());
        String pathToSave = parentPeer.getPath("restores");

        try {
            saveFile(fileInfo.getFileName(), pathToSave, fileMerge(convertHashMapToArray(chunksRestored)));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println();
        }

        // File no longer restoring
        parentPeer.setRestoring(false, fileInfo.getFileID());
    }

    private boolean sendMessageToMC(int chunkNo) {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileInfo.getFileID(),
                Integer.toString(chunkNo),
        };

        Message msg = new Message(Message.MessageType.GETCHUNK, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private ArrayList<Chunk> convertHashMapToArray(ConcurrentHashMap<String, Chunk> chunksRestored) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < fileInfo.getNumChunks(); i++) {
            chunks.add(chunksRestored.get(Integer.toString(i)));
        }
        return chunks;
    }

}
