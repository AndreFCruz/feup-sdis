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
import static protocols.ProtocolSettings.MAXFILESCHUNKS;
import static protocols.ProtocolSettings.MAXREPLICATIONDEGREE;

public class BackupInitiator implements Runnable {

    private byte[] fileData;
    private int replicationDegree;
    private File file;
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

        String fileID = generateFileID(file);
        ArrayList<Chunk> chunks = fileSplit(fileData, fileID, replicationDegree);

        if(!validBackup(replicationDegree, chunks.size())){
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
            e.printStackTrace();
        }

        Log.logWarning("Finished backupInitiator!");
    }

    private boolean validBackup(int replicationDegree, int size) {
        if(replicationDegree > MAXREPLICATIONDEGREE) {
            Log.logError("Backup: Failed replication degree greater than 9");
            return false;
        }

        if(size > MAXFILESCHUNKS){
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
        parentPeer.getDatabase().addRestorableFile(new FileInfo(file, fileID, replicationDegree, chunkInfoArray));
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
