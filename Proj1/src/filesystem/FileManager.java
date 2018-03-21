package filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {
    private String fileID;
    private String pathname;
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


}
