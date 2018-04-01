package filesystem;

import service.Peer;
import utils.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static filesystem.Database.loadDatabase;
import static java.util.Arrays.copyOfRange;
import static protocols.ProtocolSettings.MAX_CHUNK_SIZE;

public class SystemManager {
    public enum SAVE_STATE {
        EXISTS,
        SUCCESS,
        FAILURE
    }

    public static final String FILES = "../files/";
    private static final String CHUNKS = "chunks/";
    private static final String RESTORES = "restores/";
    private static long maxMemory;
    private static long usedMemory;
    private Peer parentPeer;
    private String rootPath;
    private Database database;

    public SystemManager(Peer parentPeer, long maxMemory) {
        this.parentPeer = parentPeer;
        SystemManager.maxMemory = maxMemory;

        usedMemory = 0;
        rootPath = "fileSystem/Peer" + parentPeer.getID() + "/";

        initializePeerFileSystem();

        try {
            initializeDatabase();
        } catch (IOException e) {
            Log.logError("Failed DB construction");
            e.printStackTrace();
        }
    }

    public static void createFolder(String name) {
        try {
            Files.createDirectories(Paths.get(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static SAVE_STATE saveFile(String fileName, String pathname, byte[] data) throws IOException {
        if (getAvailableMemory() < data.length) {
            Log.logWarning("Not enough space for saveFile!");
            return SAVE_STATE.FAILURE;
        }
        String filePath = pathname + "/" + fileName;

        if (Files.exists(Paths.get(filePath))) {
            Log.logWarning("File already exists");
            return SAVE_STATE.EXISTS;
        }

        OutputStream out = Files.newOutputStream(Paths.get(filePath));
        out.write(data);
        out.close();

        SystemManager.increaseUsedMemory(data.length);
        return SAVE_STATE.SUCCESS;
    }

    synchronized public static byte[] loadFile(String pathname) throws FileNotFoundException {
        InputStream inputStream = null;
        long fileSize = 0;
        byte[] data = null;

        try {
            inputStream = Files.newInputStream(Paths.get(pathname));
            fileSize = getFileSize(Paths.get(pathname));
        } catch (IOException e) {
//            e.printStackTrace();
            Log.logError("File not found!");
            return data;
        }

        data = new byte[(int) fileSize];

        try {
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static long getFileSize(Path filepath){
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(filepath, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return attr.size();
    }
    public static ArrayList<Chunk> loadChunks(String pathname, int numberOfChunks) throws FileNotFoundException {
        ArrayList<Chunk> chunks = new ArrayList<>();

        for (int i = 0; i <= numberOfChunks; i++) {
            byte[] data = loadFile(pathname + "/" + i);
            Chunk chunk = new Chunk("", i, 1, data);
            chunks.add(chunk);
        }

        return chunks;
    }

    public static ArrayList<Chunk> splitFileInChunks(byte[] fileData, String fileID, int replicationDegree) {
        ArrayList<Chunk> chunks = new ArrayList<>();

        int numChunks = fileData.length / MAX_CHUNK_SIZE + 1;

        for (int i = 0; i < numChunks; i++) {
            byte[] chunkData;

            if (i == numChunks - 1 && fileData.length % MAX_CHUNK_SIZE == 0) {
                chunkData = new byte[0];
            } else if (i == numChunks - 1) {
                int leftOverBytes = fileData.length - (i * MAX_CHUNK_SIZE);
                chunkData = copyOfRange(fileData, i * MAX_CHUNK_SIZE, i * MAX_CHUNK_SIZE + leftOverBytes);
            } else {
                chunkData = copyOfRange(fileData, i * MAX_CHUNK_SIZE, i * MAX_CHUNK_SIZE + MAX_CHUNK_SIZE);
            }

            Chunk chunk = new Chunk(fileID, i, replicationDegree, chunkData);
            chunks.add(chunk);
        }

        return chunks;
    }

    public static byte[] fileMerge(ArrayList<Chunk> chunks) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < chunks.size(); i++) {
            try {
                outputStream.write(chunks.get(i).getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputStream.toByteArray();
    }

    public static long getMaxMemory() {
        return maxMemory;
    }

    public static void setMaxMemory(int maxMemory) {
        SystemManager.maxMemory = maxMemory;
    }

    public static long getUsedMemory() {
        return SystemManager.usedMemory;
    }

    public static long getAvailableMemory() {
        return maxMemory - usedMemory;
    }

    private static void reduceUsedMemory(long n) {
        usedMemory -= n;
        if (usedMemory < 0) {
            usedMemory = 0;
            Log.logError("Used memory went below 0");
        }
    }

    private static boolean increaseUsedMemory(long n) {
        if (usedMemory + n > maxMemory) {
            Log.logWarning("Tried to surpass memory restrictions");
            return false;
        }
        usedMemory += n;
        Log.logWarning("Used memory: " + usedMemory + " / " + maxMemory);
        return true;
    }

    private void initializeDatabase() throws IOException {
        File db = new File(rootPath + "db");

        if (db.exists()) {
            database = loadDatabase(db);
        } else {
            database = new Database(db.getAbsolutePath());
        }
    }

    public String getChunkPath(String fileID, int chunkNo) {
        return getChunksPath() + fileID + "/" + chunkNo;
    }

    public byte[] loadChunk(String fileID, int chunkNo) {
        byte[] chunkData = null;
        String chunkPath = getChunkPath(fileID, chunkNo);

        try {
            //load chunk data
            chunkData = loadFile(chunkPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunkData;
    }

    private void initializePeerFileSystem() {
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

    public Database getDatabase() {
        return database;
    }

    public void deleteChunk(String fileID, int chunkNo) {
        String chunkPath = getChunkPath(fileID, chunkNo);
        Path path = Paths.get(chunkPath);

        long chunkSize = getFileSize(path);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reduceUsedMemory(chunkSize);
        database.removeChunk(fileID, chunkNo);
    }
}