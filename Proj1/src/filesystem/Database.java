package filesystem;

import utils.Log;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Database extends PermanentStateClass {
    private static final long serialVersionUID = 1L;

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
     * Contains peerIDs of mirrors of local files.
     * Maps (fileID -> Set<PeerID>)
     */
    private ConcurrentMap<String, Set<Integer>> fileMirrors;

    Database(String savePath) {
        filesBackedUp = new ConcurrentHashMap<>();
        filesByPath = new ConcurrentHashMap<>();
        chunksBackedUp = new ConcurrentHashMap<>();
        fileMirrors = new ConcurrentHashMap<>();

        this.setUp(savePath);
    }

    public void addFileMirror(String fileID, int senderID) {
        fileMirrors.putIfAbsent(fileID, new ConcurrentSkipListSet<>());
        fileMirrors.get(fileID).add(senderID);
    }

    public Set<String> getFilesToDelete(int senderID) {
        Set<String> files = new ConcurrentSkipListSet<>();

        for (Map.Entry<String, Set<Integer>> fileMirrorEntry : fileMirrors.entrySet()) {
            for (Integer mirrorID : fileMirrorEntry.getValue()) {
                if (mirrorID == senderID) {
                    files.add(fileMirrorEntry.getKey());
                    break;
                }
            }
        }

        return files;
    }

    public void deleteFileMirror(String fileID, int senderID) {
        Set<Integer> peers = fileMirrors.get(fileID);
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

    public void addChunk(ChunkInfo chunkInfo, Integer parentPeerID) {
        chunkInfo.addMirror(parentPeerID);

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

        chunksBackedUp.get(fileID).remove(chunkNo);
    }

    public Map<Integer, ChunkInfo> removeChunksBackedUpByFileID(String fileID) {
        if (!chunksBackedUp.containsKey(fileID))
            return null;

        return chunksBackedUp.remove(fileID);
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
     * @return True if the peerID was a mirror, False if it wasn't, null if ChunkData was not found
     */
    public Boolean removeChunkMirror(String fileID, int chunkNo, int peerID) {
        boolean ret;
        try {
            ret = chunksBackedUp.get(fileID).get(chunkNo).removeMirror(peerID);
        } catch (NullPointerException e) {
            Log.logError("(removeChunkMirror) ChunkData not found: " + e.getMessage());
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

    public Collection<FileInfo> getFilesBackedUp() {
        return filesBackedUp.values();
    }

    public ConcurrentMap<String, ConcurrentMap<Integer, ChunkInfo>> getChunksBackedUp() {
        return chunksBackedUp;
    }

    /**
     * Getter for any one ChunkData to be removed for reclaiming memory space.
     *
     * @return The chosen ChunkData.
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
        super.finalize();
    }

}
