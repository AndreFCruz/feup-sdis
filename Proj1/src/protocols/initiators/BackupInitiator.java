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
import java.util.List;

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

        addRestorableFile(chunks);
        parentPeer.getPeerData().startChunkReplication(fileID, chunks.size());

        ArrayList<Thread> helperThreads = new ArrayList<>(chunks.size());
        for (Chunk chunk : chunks) {
            Thread t = new Thread(new BackupChunkHelper(this, chunk));
            helperThreads.add(t);
            t.start();
        }

        try {
            joinWithThreads(helperThreads);
            parentPeer.getPeerData().resetChunkReplication(fileID);
        } catch (InterruptedException e) {
            Log.logError("Backup: Failed join with helper threads");
            e.printStackTrace();
        }

        Log.logWarning("Finished backupInitiator!");
    }

    private void addRestorableFile(ArrayList<Chunk> chunks) {
        ChunkInfo[] chunkInfoArray = new ChunkInfo[chunks.size()];
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            chunkInfoArray[i] = new ChunkInfo(chunk.getChunkNo(), chunk.getReplicationDegree());
        }
        parentPeer.addRestorableFile(file.getPath(), new FileInfo(file, fileID, replicationDegree, chunkInfoArray));
    }

    private void joinWithThreads(List<Thread> threads) throws InterruptedException {
        for (Thread t : threads) {
            t.join();
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
