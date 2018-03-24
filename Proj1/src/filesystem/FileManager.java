package filesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;
import static utils.Utils.MAXCHUNK;

public class FileManager {
    private String fileID;
    private String pathname;

    private byte[] fileData;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    public FileManager(String fileID, String pathname) {
        this.fileID = fileID;
        this.pathname = pathname;
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

    public static ArrayList<Chunk> fileSplit(byte[] fileData, String fileID, int replicationDegree) {
        ArrayList<Chunk> chunks = new ArrayList<>();

        int numChunks = fileData.length / MAXCHUNK + 1;

        for (int i = 0; i < numChunks; i++) {
            byte[] chunkData;

            if (i == numChunks - 1 && fileData.length % MAXCHUNK == 0) {
                chunkData = new byte[0];
            } else if(i == numChunks - 1){
                int leftOverBytes = fileData.length - (i * 64000);
                chunkData = copyOfRange(fileData, i*MAXCHUNK,i*MAXCHUNK + leftOverBytes);
            } else{
                chunkData = copyOfRange(fileData, i*MAXCHUNK,i*MAXCHUNK + MAXCHUNK);
            }

            Chunk chunk = new Chunk(fileID, i, replicationDegree, chunkData);
            chunks.add(chunk);
        }

        return chunks;
    }

    public static byte[] fileMerge(ArrayList<Chunk> chunks){
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

}

