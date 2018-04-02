package protocols.initiators;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import filesystem.SystemManager;
import protocols.initiators.helpers.BackupChunkHelper;
import service.Peer;
import utils.Log;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static filesystem.SystemManager.splitFileInChunks;
import static protocols.ProtocolSettings.MAX_NUM_CHUNKS;
import static protocols.ProtocolSettings.MAX_REPLICATION_DEGREE;

public class BackupInitiator implements Runnable {

    private byte[] fileData;
    private int replicationDegree;
    private String pathname;
    private Peer parentPeer;
    private String version;

    public BackupInitiator(String version, String pathname, int replicationDegree, Peer parentPeer) {
        this.version = version;
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        this.parentPeer = parentPeer;

        Log.logWarning("Starting backupInitiator!");
    }

    @Override
    public void run() {
        fileData = SystemManager.loadFile(pathname);

        String fileID = generateFileID(pathname);
        ArrayList<Chunk> chunks = splitFileInChunks(fileData, fileID, replicationDegree);

        if (!validBackup(replicationDegree, chunks.size())) {
            return;
        }

        addRestorableFile(chunks, fileID);
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
        }

        Log.logWarning("Finished BackupInitiator!");
    }

    private boolean validBackup(int replicationDegree, int size) {
        if (replicationDegree > MAX_REPLICATION_DEGREE) {
            Log.logError("Backup: Failed replication degree greater than 9");
            return false;
        }

        if (size > MAX_NUM_CHUNKS) {
            Log.logError("Backup: Failed file size greater than 64GB");
            return false;
        }

        return true;
    }

    private void addRestorableFile(ArrayList<Chunk> chunks, String fileID) {
        ChunkInfo[] chunkInfoArray = new ChunkInfo[chunks.size()];
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            chunkInfoArray[i] = new ChunkInfo(chunk.getChunkNo(), chunk.getReplicationDegree());
        }
        parentPeer.getDatabase().addRestorableFile(new FileInfo(pathname, fileID, replicationDegree, chunkInfoArray));
    }

    private void joinWithThreads(List<Thread> threads) throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }

    private String generateFileID(String pathname) {
        return Utils.hash(generateUnhashedFileID(pathname));
    }

    private String generateUnhashedFileID(String pathname) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(Paths.get(pathname), BasicFileAttributes.class);
        } catch (IOException e) {
            Log.logError("Couldn't read file's metadata: " + e.getMessage());
            return null;
        }

        Path filepath = Paths.get(pathname);
        String fileID = filepath.getFileName().toString() + attr.lastModifiedTime() + attr.size();
        return fileID;
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public String getProtocolVersion() {
        return version;
    }
}
