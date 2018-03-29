package filesystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Database {

    /**
     * Contains files readye that were backed up locally,
     * and may be restored.
     * Maps (pathname -> FileInfo)
     */
    private ConcurrentMap<String, FileInfo> filesBackedUp;

    /**
     * Contains backed up Chunks (on disk memory).
     * Maps (fileID -> (ChunkNum -> Chunk))
     */
    private ConcurrentMap<String, ConcurrentHashMap<Integer, ChunkInfo>> chunksBackedUp;

    // TODO _inner_ String to Int, and _inner_ ConcurrentHashMap to ConcurrentSkipListMap
    /**
     * Contains the in-memory chunks restored.
     * Maps (fileID -> (ChunkNum -> Chunk))
     */
    private ConcurrentMap<String, ConcurrentHashMap<String, Chunk>> chunksRestored;
//    private ConcurrentMap<String, ConcurrentSkipListMap<String, Chunk>> chunksRestored;


    public Database() {
        filesBackedUp = new ConcurrentHashMap<>();
        chunksBackedUp = new ConcurrentHashMap<>();
        chunksRestored = new ConcurrentHashMap<>();

        initializeDatabase();
    }

    private void initializeDatabase() {
        //TODO: Load from metadata from files the previous backups and chunks

        //TODO: unserialize self?
    }

    private void saveDatabase() {
        //TODO: Save metadata to files

        //TODO: or serialize self?
    }

    public void setFlagRestored(boolean flag, String fileID) {
        if (flag) {
            chunksRestored.put(fileID, new ConcurrentHashMap<>());
        } else {
            chunksRestored.remove(fileID);
        }
    }

    public boolean getFlagRestored(String fileID) {
        return chunksRestored.containsKey(fileID);
    }

    public void addChunksRestored(Chunk chunk) {
        if (chunksRestored.get(chunk.getFileID()).containsKey(Integer.toString(chunk.getChunkNo()))) {
            System.out.println("Chunk already exist");
        } else {
            System.out.println("Adding chunk to merge");
            chunksRestored.get(chunk.getFileID()).put(Integer.toString(chunk.getChunkNo()), chunk);
        }

    }

    public Integer getChunksRestoredSize(String fileID) {
        return chunksRestored.get(fileID).size();
    }

    public ConcurrentHashMap<String, Chunk> getChunksRestored(String fileID) {
        return chunksRestored.get(fileID);
    }

    public boolean hasRestoreFinished(String pathName, String fileID) {
        int numChunks = filesBackedUp.get(pathName).getNumChunks();
        int chunksRestored = getChunksRestoredSize(fileID);

        return numChunks == chunksRestored;
    }

    /*
     *
     */
    //Backup
    public void addRestorableFile(String pathName, FileInfo fileInfo) {
        filesBackedUp.put(pathName, fileInfo);
        saveDatabase();
    }

    //delete
    public void removeRestorableFile(String pathName) {
        filesBackedUp.remove(pathName);
        saveDatabase();
    }

    public boolean hasFile(String pathName) {
        return filesBackedUp.containsKey(pathName);
    }

    public FileInfo getFileInfo(String pathName) {
        return filesBackedUp.get(pathName);
    }

    /*
     *
     */
    public boolean hasChunk(String fileID, int chunkNo) {
        Map<Integer, ChunkInfo> fileChunks = chunksBackedUp.get(fileID);
        return fileChunks != null && fileChunks.containsKey(chunkNo);
    }

    public void addChunk(ChunkInfo chunkInfo) {
        String fileID = chunkInfo.getFileID();
        int chunkNo = chunkInfo.getChunkNo();

        Map<Integer, ChunkInfo> fileChunks;
        fileChunks = chunksBackedUp.getOrDefault(fileID, new ConcurrentHashMap<>());

        fileChunks.putIfAbsent(chunkNo, chunkInfo);

        saveDatabase();
    }

    public ChunkInfo getChunkInfo(String fileID, int chunkNo) {
        Map<Integer, ChunkInfo> fileChunks = chunksBackedUp.get(fileID);

        return fileChunks != null ? fileChunks.get(chunkNo) : null;
    }

    public void removeChunk(String fileID, int chunkNo) {
        if (! chunksBackedUp.containsKey(fileID))
            return;

        chunksBackedUp.get(fileID).remove(chunkNo);
        saveDatabase();
    }

    // TODO addChunkMirror, removeChunkMirror, getChunkMirrorsSize ?

}
