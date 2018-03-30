package filesystem;

import utils.Log;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Database implements Serializable {

    /**
     * Contains local files that were backed up,
     * and may be restored.
     * Maps (fileID -> FileInfo)
     */
    private ConcurrentMap<String, FileInfo> filesBackedUp;

    /**
     * Maps (filePath -> FileInfo)
     */
    private ConcurrentMap<String, FileInfo> filesByPath;

    /**
     * Contains backed up Chunks (on disk memory).
     * Maps (fileID -> (ChunkNum -> ChunkInfo))
     */
    private ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> chunksBackedUp;


    public Database() {
        filesBackedUp = new ConcurrentHashMap<>();
        filesByPath = new ConcurrentHashMap<>();
        chunksBackedUp = new ConcurrentHashMap<>();
    }

    //Load and store database
    synchronized public static void saveDatabase(final Database pDB, File file) {
//        try {
//            final ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
//            outputStream.writeObject(pDB);
//            outputStream.close();
//        } catch (final IOException pE) {
//            Log.logError("Couldn't save database!");
//            pE.printStackTrace();
//        }
    }

    synchronized public static Database loadDatabase(File file) {
        Database db = null;

        try {
            final ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            db = (Database) inputStream.readObject();
            inputStream.close();
        } catch (final IOException pE) {
            Log.logError("Couldn't load database!");
            pE.printStackTrace();
        } catch (ClassNotFoundException pE) {
            Log.logError("Class was removed since last execution!?");
            pE.printStackTrace();
        }

        return db;
    }

    public void addRestorableFile(FileInfo fileInfo) {
        filesBackedUp.put(fileInfo.getFileID(), fileInfo);
        filesByPath.put(fileInfo.getPathname(), fileInfo);
//        saveDatabase();
    }

    public void removeRestorableFileByPath(String pathName) {
        filesByPath.remove(pathName);
//        saveDatabase();
    }

    public boolean hasBackedUpFileById(String fileID) {
        return filesBackedUp.containsKey(fileID);
    }

    public boolean hasBackedUpFileByPath(String path) {
        return filesByPath.containsKey(path);
    }

    public FileInfo getFileInfoByPath(String pathName) {
        return filesByPath.get(pathName);
    }

    // chunksBackedUp
    public boolean hasChunk(String fileID, int chunkNo) {
        Map<Integer, ChunkInfo> fileChunks = chunksBackedUp.get(fileID);

        return fileChunks != null && fileChunks.containsKey(chunkNo);
    }

    public void addChunk(ChunkInfo chunkInfo) {
        String fileID = chunkInfo.getFileID();
        int chunkNo = chunkInfo.getChunkNo();

        ConcurrentMap<Integer, ChunkInfo> fileChunks;
        fileChunks = chunksBackedUp.getOrDefault(fileID, new ConcurrentHashMap<>());
        fileChunks.putIfAbsent(chunkNo, chunkInfo);

        chunksBackedUp.putIfAbsent(fileID, fileChunks);

//        saveDatabase();
    }

    public ChunkInfo getChunkInfo(String fileID, int chunkNo) {
        Map<Integer, ChunkInfo> fileChunks = chunksBackedUp.get(fileID);

        return fileChunks != null ? fileChunks.get(chunkNo) : null;
    }

    public void removeChunk(String fileID, int chunkNo) {
        if (!chunksBackedUp.containsKey(fileID))
            return;

        chunksBackedUp.get(fileID).remove(chunkNo);
//        saveDatabase();
    }

    public void removeFileBackedUp(String fileID) {
        if (!chunksBackedUp.containsKey(fileID))
            return;

        chunksBackedUp.remove(fileID);
//        saveDatabase();
    }

    public int getNumChunks(String pathname) {
        return filesBackedUp.get(pathname).getNumChunks();
    }

    public Boolean addChunkMirror(String fileID, int chunkNo, int peerID) {
        boolean ret;
        try {
            ret = chunksBackedUp.get(fileID).get(chunkNo).addMirror(peerID);
        } catch (NullPointerException e) {
            Log.logError("addChunkMirror " + e.getMessage());
            return null;
        }

        return ret;
    }

    /**
     * Removes the given peerID as a mirror of given chunk
     * @param fileID The chunk's fileID
     * @param chunkNo The chunk's id number
     * @param peerID The peerID to be removed
     * @return True if the peerID was a mirror, False if it wasn't, null if Chunk was not found
     */
    public Boolean removeChunkMirror(String fileID, int chunkNo, int peerID) {
        boolean ret;
        try {
            ret = chunksBackedUp.get(fileID).get(chunkNo).removeMirror(peerID);
        } catch (NullPointerException e) {
            Log.logWarning("(removeChunkMirror) Chunk not found: " + e.getMessage());
            return null;
        }

        return ret;
    }

    public Integer getChunkPerceivedReplication(String fileID, int chunkNo) {
        int ret;
        try {
            ret = chunksBackedUp.get(fileID).get(chunkNo).getNumMirrors();
        } catch (NullPointerException e) {
            Log.logError("getChunkPerceivedReplication " + e.getMessage());
            return null;
        }

        return ret;
    }

    public boolean hasChunks(String fileID) {
        return chunksBackedUp.containsKey(fileID);
    }

    public Set<Integer> getFileChunksKey(String fileID) {
        return chunksBackedUp.get(fileID).keySet();
    }

    public ConcurrentMap<String, FileInfo> getFilesBackedUp() {
        return filesBackedUp;
    }

    public ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> getChunksBackedUp() {
        return chunksBackedUp;
    }

    /**
     * Getter for any one Chunk to be removed for reclaiming memory space.
     * @return The chosen Chunk.
     */
    public ChunkInfo getChunkForRemoval() {
        // Currently, chunk for removal is most backed-up chunk
        return getMostBackedUpChunk();
    }

    private ChunkInfo getMostBackedUpChunk() {
        ChunkInfo mostBackedUpChunk = null;
        int maxMirroring = -1;

        for (ConcurrentMap.Entry<String, ConcurrentMap<Integer, ChunkInfo>> fileEntry : chunksBackedUp.entrySet()) {
            for (ConcurrentMap.Entry<Integer, ChunkInfo> chunkEntry : fileEntry.getValue().entrySet()) {
                int numMirrors = chunkEntry.getValue().getNumMirrors();
                if (numMirrors > maxMirroring) {
                    maxMirroring = numMirrors;
                    mostBackedUpChunk = chunkEntry.getValue();
                }
            }
        }

        return mostBackedUpChunk;
    }
}
