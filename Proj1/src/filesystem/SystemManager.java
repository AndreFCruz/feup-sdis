package filesystem;

import service.Peer;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class SystemManager {

//    private ConcurrentHashMap<String, FileManager> storage;
//
//    public SystemManager(ConcurrentHashMap<String, FileManager> storage) {
//        this.storage = storage;
//    }
    public static final String FILES = "../files/";

    private static final String CHUNKS = "chunks/";

    private static final String RESTORES = "restores/";

    private long maxMemory;

    private long usedMemory;

    private Peer parentPeer;

    private String rootPath;

    public SystemManager(Peer parentPeer, long maxMemory) {
        this.parentPeer = parentPeer;
        this.maxMemory = maxMemory;
        usedMemory = 0;
        rootPath = "fileSystem/Peer" + parentPeer.getID() + "/";
        initializePeerFS();
    }

    private void initializePeerFS() {
        createFolder(rootPath + CHUNKS);
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getChunksPath() {
        return rootPath + CHUNKS;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public boolean fileExists(String name) {
        File file = new File(name);

        return file.exists() && file.isFile();
    }

    public boolean folderExists(String name) {
        File file = new File(name);

        return file.exists() && file.isDirectory();
    }

    public void createFolder(String name) {
        File file = new File(name);

        file.mkdirs();
    }
}
