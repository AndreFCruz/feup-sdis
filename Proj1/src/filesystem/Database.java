package filesystem;

import utils.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

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

    /**
     * Contains peerIDs to delete a file.
     * Maps (fileID -> Array<PeerID>)
     */
    private ConcurrentMap<String, Set<Integer>> filesToDelete;

    /**
     * Stream used to serialize this instance.
     * Is static to not be serialized with the class instance.
     */
    private static ObjectOutputStream objectOutputStream;

    /**
     * Period between DB saves, in milliseconds
     */
    private final long SAVE_PERIOD = 1000;

    private String savePath;


    Database(String savePath) throws IOException {
        filesBackedUp = new ConcurrentHashMap<>();
        filesByPath = new ConcurrentHashMap<>();
        chunksBackedUp = new ConcurrentHashMap<>();
        filesToDelete = new ConcurrentHashMap<>();

        setUpDatabase(savePath);
    }

    public void setUpDatabase(String savePath) throws IOException {
        this.savePath = savePath;

        OutputStream out = new FileOutputStream(savePath);
        objectOutputStream = new ObjectOutputStream(out);

        setUpPeriodicSaves();
    }

    private void setUpPeriodicSaves() {
        Database db = this;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                db.savePermanentState();
            }
        }, SAVE_PERIOD, SAVE_PERIOD);
    }

    synchronized private void savePermanentState() {
        try {
            File file = new File(this.savePath);
            file.delete();
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            Log.logError("Couldn't save database");
            e.printStackTrace();
        }
    }

    synchronized static Database loadDatabase(File file) throws IOException, ClassNotFoundException {
        Database db;

        FileInputStream fileIn = new FileInputStream(file);
        final ObjectInputStream inputStream = new ObjectInputStream(fileIn);
        db = (Database) inputStream.readObject();
        inputStream.close();
        fileIn.close();

        if (db != null)
            db.setUpDatabase(file.getAbsolutePath());
        else
            Log.logError("Error initializing DB from file");

        return db;
    }

    public void  addFileMirror(String fileID, int senderID) {
        filesToDelete.putIfAbsent(fileID, new ConcurrentSkipListSet<>());
        Set<Integer> peers = filesToDelete.get(fileID);
        peers.add(senderID);
    }

    public Set<String> getFilesToDelete(int senderID){
        Set<String> files = new ConcurrentSkipListSet<>();

        for (Map.Entry<String, Set<Integer>> outer : filesToDelete.entrySet()) {
            for (Integer inner : outer.getValue()) {
                if(inner == senderID){
                    files.add(outer.getKey());
                    break;
                }
            }
        }

        return files;
    }

    public void  deleteFileMirror(String fileID, int senderID) {
        Set<Integer> peers = filesToDelete.get(fileID);
        if (peers != null)
            peers.remove(senderID);
    }

    public void addRestorableFile(FileInfo fileInfo) {
        filesBackedUp.put(fileInfo.getFileID(), fileInfo);
        filesByPath.put(fileInfo.getPath(), fileInfo);
    }

    public void removeRestorableFile(FileInfo fileInfo) {
        filesBackedUp.remove(fileInfo.getFileID());
        filesByPath.remove(fileInfo.getPath());
    }

    public void removeRestorableFileByPath(String path) {
        removeRestorableFile(filesByPath.get(path));
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
    }

    public ChunkInfo getChunkInfo(String fileID, int chunkNo) {
        Map<Integer, ChunkInfo> fileChunks = chunksBackedUp.get(fileID);

        return fileChunks != null ? fileChunks.get(chunkNo) : null;
    }

    public void removeChunk(String fileID, int chunkNo) {
        if (!chunksBackedUp.containsKey(fileID))
            return;

        Log.log("Apaguei! " + fileID + " " +chunkNo);
        chunksBackedUp.get(fileID).remove(chunkNo);
    }

    public void removeFileBackedUp(String fileID) {
        if (!chunksBackedUp.containsKey(fileID))
            return;

        chunksBackedUp.remove(fileID);
    }

    public int getNumChunksByFilePath(String path) {
        return filesByPath.get(path).getNumChunks();
    }

    public String getFileInfoByFileID(String fileID) {
        return filesBackedUp.get(fileID).getPath();
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
     * Removes the given peerID as a mirror of the specified chunk.
     *
     * @param fileID  The chunk's fileID
     * @param chunkNo The chunk's id number
     * @param peerID  The peerID to be removed
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

    public Collection<FileInfo> getFilesBackedUp() {
        return filesBackedUp.values();
    }

    public ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> getChunksBackedUp() {
        return chunksBackedUp;
    }

    /**
     * Getter for any one Chunk to be removed for reclaiming memory space.
     *
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

    @Override
    protected void finalize() throws Throwable {
        savePermanentState();
        objectOutputStream.close();
        super.finalize();
    }
    

}
