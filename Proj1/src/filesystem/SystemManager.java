package filesystem;

import service.Peer;
import utils.Log;

import java.io.*;
import java.util.ArrayList;

import static filesystem.Database.loadDatabase;
import static java.util.Arrays.copyOfRange;
import static protocols.ProtocolSettings.MAX_CHUNK_SIZE;

public class SystemManager {

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

        initializeDatabase();
        initializePeerFS();
    }

    public static void createFolder(String name) {
        File file = new File(name);
        file.mkdirs();
    }

    synchronized public static SAVE_STATE saveFile(String fileName, String pathname, byte[] data) throws IOException {
        if (getAvailableMemory() < data.length) {
            Log.logWarning("Not enough space for saveFile!");
            return SAVE_STATE.FAILURE;
        }
        String filePath = pathname + "/" + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            Log.logWarning("File already exists");
            return SAVE_STATE.EXISTS;
        }

        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(data);
        out.close();

        SystemManager.increaseUsedMemory(data.length);
        return SAVE_STATE.SUCCESS;
    }

    synchronized public static byte[] loadFile(File file) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);

        byte[] data = new byte[(int) file.length()];

        try {
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static ArrayList<Chunk> loadChunks(String pathname, int numberOfChunks) throws FileNotFoundException {
        ArrayList<Chunk> chunks = new ArrayList<>();

        for (int i = 0; i <= numberOfChunks; i++) {
            byte[] data = loadFile(new File(pathname + "/" + i));
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

    private void initializeDatabase() {
        File db = new File(rootPath + "db");

        if (db.exists()) {
            database = loadDatabase(db);
        } else {
            database = new Database();
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
            chunkData = loadFile(new File(chunkPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunkData;
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

    public Database getDatabase() {
        return database;
    }

    public void deleteChunk(String fileID, int chunkNo) {
        String chunkPath = getChunkPath(fileID, chunkNo);
        File file = new File(chunkPath);

        long chunkSize = file.length();
        file.delete();
        reduceUsedMemory(chunkSize);
        database.removeChunk(fileID, chunkNo);
    }

    public enum SAVE_STATE {
        EXISTS,
        SUCCESS,
        FAILURE
    }
}