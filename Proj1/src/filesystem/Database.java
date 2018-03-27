package filesystem;

import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private ConcurrentHashMap<String, FileInfo> restorableFiles; //pathname, fileinfo
    private ConcurrentHashMap<String, ChunkInfo> chunksBackedUp; //chunkid, chunkinfo

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
    public void addRestorableFile(String pathName, FileInfo fileInfo) {
        if (!hasChunk(pathName)) {
            restorableFiles.put(pathName, fileInfo);

            saveDatabase();
        }
    }

    //delete
    public void removeRestorableFile(String pathName) {
        restorableFiles.remove(pathName);

        saveDatabase();
    }

    public boolean hasFile(String pathName) {
        return restorableFiles.containsKey(pathName);
    }

    public FileInfo getFileInfo(String pathName) {
        return restorableFiles.get(pathName);
    }

    /*
     *
     */
    public boolean hasChunk(String chunkID) {
        return chunksBackedUp.containsKey(chunkID);
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
