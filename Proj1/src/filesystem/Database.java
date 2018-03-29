package filesystem;

import utils.Log;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Database implements Serializable {

    /**
     * Contains files that were backed up locally,
     * and may be restored.
     * Maps (pathname -> FileInfo)
     */
    private ConcurrentMap<String, FileInfo> filesBackedUp;

    /**
     * Contains backed up Chunks (on disk memory).
     * Maps (fileID -> (ChunkNum -> ChunkInfo))
     */
    private ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> chunksBackedUp;


    public Database() {
        filesBackedUp = new ConcurrentHashMap<>();
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

    // filesBackedUp
    public void addRestorableFile(String pathName, FileInfo fileInfo) {
        filesBackedUp.put(pathName, fileInfo);
//        saveDatabase();
    }

    public void removeRestorableFile(String pathName) {
        filesBackedUp.remove(pathName);
//        saveDatabase();
    }

    public boolean hasFile(String pathName) {
        return filesBackedUp.containsKey(pathName);
    }

    public FileInfo getFileInfo(String pathName) {
        return filesBackedUp.get(pathName);
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

    public Boolean removeChunkMirror(String fileID, int chunkNo, int peerID) {
        boolean ret;
        try {
            ret = chunksBackedUp.get(fileID).get(chunkNo).removeMirror(peerID);
        } catch (NullPointerException e) {
            Log.logError("removeChunkMirror " + e.getMessage());
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
}
