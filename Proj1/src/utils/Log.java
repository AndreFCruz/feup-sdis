package utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static PrintStream logFile = System.err;
    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    synchronized public static void logError(String msg) {
        Date date = new Date();

        logFile.println("Error   @ " + dateFormat.format(date) + " : " + msg);
        logFile.flush();
    }

    synchronized public static void logWarning(String msg) {
        Date date = new Date();

        logFile.println("Warning @ " + dateFormat.format(date) + " : " + msg);
        logFile.flush();
    }

    synchronized public static void log(String msg) {
        Date date = new Date();

        logFile.println("Log     @ " + dateFormat.format(date) + " : " + msg);
        logFile.flush();
    }

    synchronized public static void setLogFile(String filepath) throws FileNotFoundException {
        Log.logFile = new PrintStream(new FileOutputStream(filepath, true));
    }
}
