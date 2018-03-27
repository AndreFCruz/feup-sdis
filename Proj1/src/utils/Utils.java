package utils;

public class Utils {
    public static final String CRLF = "" + (char) 0x0D + (char) 0x0A;

    public static final int MAXCHUNK = 64000;

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK
    }

}
