package filesystem;

import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private ConcurrentHashMap<String, FileInfo> restorableFiles;
    private ConcurrentHashMap<String, ChunkInfo> chunksBackedUp;

    public Database() {
        restorableFiles = new ConcurrentHashMap<>();
        chunksBackedUp = new ConcurrentHashMap<>();
        initializeDatabase();
    }

    private void initializeDatabase() {
        //TODO: Load from metadata from files the previous backups and chunks
        //maybe usar json
    }

    private void saveDatabase() {
        //TODO: Save metadata to files
    }


    /*
     *
     */
    //Backup
    public void addRestorableFile(String fileName, FileInfo fileInfo) {
        if (!hasChunk(fileName)) {
            restorableFiles.put(fileName, fileInfo);

            saveDatabase();
        }
    }

    //delete
    public void removeRestorableFile(String fileName) {
        restorableFiles.remove(fileName);

        saveDatabase();
    }

    public boolean hasFile(String fileName) {
        return restorableFiles.containsKey(fileName);
    }

    public FileInfo getFileInfo(String fileName) {
        return restorableFiles.get(fileName);
    }

    /*
     *
     */
    public boolean hasChunk(String String) {
        return chunksBackedUp.containsKey(String);
    }

    public void addChunk(String chunkID, ChunkInfo chunkInfo) {
        if (!hasChunk(chunkID)) {
            chunksBackedUp.put(chunkID, chunkInfo);

            saveDatabase();
        }
    }

    public void removeChunk(String chunkID) {
        chunksBackedUp.remove(chunkID);

        saveDatabase();
    }

    public void addChunkMirror(String chunkID, String peerID) {
        if (hasChunk(chunkID)) {
            if (!chunksBackedUp.get(chunkID).getMirrors().contains(peerID)) {
                chunksBackedUp.get(chunkID).getMirrors().add(peerID);

                saveDatabase();
            }
        }
    }

    public void removeChunkMirror(String chunkID, String peerID) {
        chunksBackedUp.get(chunkID).removeMirror(peerID);
    }

    public int getChunkReplicationDegree(String chunkID) {
        return chunksBackedUp.get(chunkID).getReplicationDegree();
    }

    public int getChunkMirrorsSize(String chunkID) {
        return chunksBackedUp.get(chunkID).getMirrors().size();
    }

}
