package filesystem;

import service.Peer;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.copyOfRange;
import static utils.Utils.MAXCHUNK;

public class SystemManager {

    public static final String FILES = "../files/";

    private static final String CHUNKS = "chunks/";

    private static final String RESTORES = "restores/";

    private long maxMemory;

    private long usedMemory;

    private Peer parentPeer;

    private String rootPath;

    private Database database;

    public SystemManager(Peer parentPeer, long maxMemory) {
        this.parentPeer = parentPeer;
        this.maxMemory = maxMemory;

        usedMemory = 0;
        rootPath = "fileSystem/Peer" + parentPeer.getID() + "/";

        database = new Database();

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

    public static final byte[] loadFile(File file) throws FileNotFoundException {
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

    public static ArrayList<Chunk> fileSplit(byte[] fileData, String fileID, int replicationDegree) {
        ArrayList<Chunk> chunks = new ArrayList<>();

        int numChunks = fileData.length / MAXCHUNK + 1;

        for (int i = 0; i < numChunks; i++) {
            byte[] chunkData;

            if (i == numChunks - 1 && fileData.length % MAXCHUNK == 0) {
                chunkData = new byte[0];
            } else if (i == numChunks - 1) {
                int leftOverBytes = fileData.length - (i * MAXCHUNK);
                chunkData = copyOfRange(fileData, i * MAXCHUNK, i * MAXCHUNK + leftOverBytes);
            } else {
                chunkData = copyOfRange(fileData, i * MAXCHUNK, i * MAXCHUNK + MAXCHUNK);
            }

            Chunk chunk = new Chunk(fileID, i, replicationDegree, chunkData);
            chunks.add(chunk);
        }

        return chunks;
    }

    public static byte[] fileMerge(ArrayList<Chunk> chunks) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] fileData;

        for (int i = 0; i < chunks.size(); i++) {
            try {
                outputStream.write(chunks.get(i).getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileData = outputStream.toByteArray();

        return fileData;
    }


    public Database getDatabase() {
        return database;
    }
}
