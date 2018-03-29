package utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Log {
    private static PrintStream logFile = System.err;

    public static void logError(String msg) {
        logFile.println("Error   @ " + System.currentTimeMillis() + " : " + msg);
        logFile.flush();
    }

    public static void logWarning(String msg) {
        logFile.println("Warning @ " + System.currentTimeMillis() + " : " + msg);
        logFile.flush();
    }

    public static void setLogFile(String filepath) throws FileNotFoundException {
        Log.logFile = new PrintStream(new FileOutputStream(filepath, true));
    }
}
