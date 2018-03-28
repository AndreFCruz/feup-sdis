package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import filesystem.SystemManager;
import network.Message;
import service.Peer;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
            fileID = generateFileID(file);
            ArrayList<Chunk> chunks = fileSplit(fileData, fileID, replicationDegree);
            ConcurrentHashMap<String, ChunkInfo> chunksInfo = new ConcurrentHashMap<>();


            for (Chunk chunk : chunks) {
                sendMessageToMDB(chunk);
                //Save chunk info, TODO: maybe save the peers that store each chunk?
                chunksInfo.put(Integer.toString(chunk.getChunkNo()), new ChunkInfo(chunk.getChunkNo(), chunk.getReplicationDegree()));
            }

//            byte[] dataMerged = fileMerge(chunks);
//            saveFile("batatas1.png", this.parentPeer.getPath("restores"), dataMerged);

            //sendMessageToMDB();


            parentPeer.addFileToDB(file.getPath(), new FileInfo(file, fileID, replicationDegree, chunksInfo));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateFileID(File file) {
        return Utils.hash(generateUnhashedFileID(file));
    }

    private String generateUnhashedFileID(File file) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            System.err.println("Couldn't read file's metadata: " + e.getMessage());
            return null;
        }

        String fileID = file.getName() + attr.lastModifiedTime() + attr.size();

        return fileID;
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

        Message msg = new Message(Message.MessageType.PUTCHUNK, args, chunk.getData());
        parentPeer.sendMessage(Channel.ChannelType.MDB, msg);
    }

    private void uploadFile() {


    }
}
