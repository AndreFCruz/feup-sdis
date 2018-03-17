package filesystem;

import java.util.ArrayList;

public class File {
    private String fileID;
    private String pathname;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    public File(String fileID, String pathname) {
        this.fileID = fileID;
        this.pathname = pathname;
    }

}
