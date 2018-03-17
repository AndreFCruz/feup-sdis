package filesystem;

import java.util.ArrayList;

public class FileSplitter {
    private byte[] fileData;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    public FileSplitter(byte[] fileData) {
        this.fileData = fileData;
        fileSplit();
    }

    public void fileSplit(){

    }
}
