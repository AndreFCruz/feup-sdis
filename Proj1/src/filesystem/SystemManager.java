package filesystem;

import service.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        createFolder(rootPath + RESTORES);
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getChunksPath() {
        return rootPath + CHUNKS;
    }

    public String getRestoresPath() {
        return rootPath + RESTORES;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public static boolean fileExists(String name) {
        File file = new File(name);

        return file.exists() && file.isFile();
    }

    public static boolean folderExists(String name) {
        File file = new File(name);

        return file.exists() && file.isDirectory();
    }

    public static void createFolder(String name) {
        File file = new File(name);
        // TODO check if dir already exists?
        file.mkdirs();
    }

    public static final void saveFile(String fileName, String pathname, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(pathname + "/" + fileName);
        out.write(data);
        out.close();
    }
}
