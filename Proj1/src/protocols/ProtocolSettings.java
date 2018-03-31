package protocols;

public class ProtocolSettings {

    /**
     * Maximum delay for random wait-time for STORED response.
     */
    public static final int MAX_DELAY = 400;

    public static final int MAX_SYSTEM_MEMORY = (int) Math.pow(10, 6) * 8; // 8MB

    public static final int PUTCHUNK_RETRIES = 5;

    public static final int MAXCHUNK = 64000;

    public static final int MAXREPLICATIONDEGREE = 9;

    public static final int MAXFILESCHUNKS = 1000000;

    public static final String ENHANCEMENT_BACKUP = "1.1";

    public static final String ENHANCEMENT_RESTORE = "1.2";

    public static final String ENHANCEMENT_DELETE = "1.3";

    public static final String ENHANCEMENT_ALL = "2.0";

}
