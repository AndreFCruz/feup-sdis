package protocols.initiators;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import filesystem.SystemManager;
import protocols.initiators.helpers.BackupChunkHelper;
import service.Peer;
import utils.Log;
import utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

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

        Log.logWarning("Starting backupInitiator!");
    }

    @Override
    public void run() {
        try {
            fileData = SystemManager.loadFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fileID = generateFileID(file);
        ArrayList<Chunk> chunks = fileSplit(fileData, fileID, replicationDegree);
        HashMap<String, ChunkInfo> chunksInfo = new HashMap<>();

        parentPeer.getPeerData().startChunkReplication(fileID, chunks.size());

        for (Chunk chunk : chunks) {
            new Thread(new BackupChunkHelper(this, chunk)).start();
            chunksInfo.put(Integer.toString(chunk.getChunkNo()), new ChunkInfo(chunk.getChunkNo(), chunk.getReplicationDegree()));
        }

        parentPeer.addFileToDB(file.getPath(), new FileInfo(file, fileID, replicationDegree, chunksInfo));
        parentPeer.getPeerData().resetChunkReplication(fileID);

        Log.logWarning("Finished backupInitiator!");
    }

    private String generateFileID(File file) {
        return Utils.hash(generateUnhashedFileID(file));
    }

    private String generateUnhashedFileID(File file) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            Log.logError("Couldn't read file's metadata: " + e.getMessage());
            return null;
        }

        String fileID = file.getName() + attr.lastModifiedTime() + attr.size();
        return fileID;
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public String getProtocolVersion() {
        return version;
    }
}
