package protocols.initiators;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import filesystem.SystemManager;
import network.Message;
import service.Peer;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static filesystem.SystemManager.fileSplit;


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
            fileData = SystemManager.loadFile(file);
            fileID = file.getName(); //temporary (need to be hashed)
            ArrayList<Chunk> chunks = fileSplit(fileData, fileID, replicationDegree);
            ArrayList<ChunkInfo> chunksInfo = new ArrayList<>();


            for (Chunk chunk : chunks) {
                sendMessageToMDB(chunk);
                //Save chunk info, TODO: maybe save the peers that store each chunk?
                chunksInfo.add(new ChunkInfo(chunk.getChunkNo(), chunk.getReplicationDegree()));
            }

//            byte[] dataMerged = fileMerge(chunks);
//            saveFile("batatas1.png", this.parentPeer.getPath("restores"), dataMerged);

            //sendMessageToMDB();


            parentPeer.addFileToDB(file.getPath(), new FileInfo(file, fileID, replicationDegree, chunksInfo));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToMDB(Chunk chunk) throws IOException {
        System.out.println(parentPeer);

        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileID,
                Integer.toString(chunk.getChunkNo()),
                Integer.toString(replicationDegree)
        };

        Message msg = new Message(Utils.MessageType.PUTCHUNK, args, chunk.getData());

        parentPeer.sendMessage(1, msg);
    }

    private void uploadFile() {


    }
}
