package filesystem;

import utils.Log;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public abstract class PermanentStateClass implements Serializable {

    /**
     * Period between file saves, in milliseconds
     */
    private final long SAVE_PERIOD = 1000;

    private String savePath;

    static PermanentStateClass loadFromFile(File file) throws IOException, ClassNotFoundException {
        PermanentStateClass obj;

        FileInputStream fileIn = new FileInputStream(file);
        final ObjectInputStream inputStream = new ObjectInputStream(fileIn);
        obj = (PermanentStateClass) inputStream.readObject();
        inputStream.close();
        fileIn.close();

        if (obj != null)
            obj.setUp(file.getAbsolutePath());
        else
            Log.logError("Error initializing PermanentStateClass from file");

        return obj;
    }

    protected void setUp(String absPath) {
        this.savePath = absPath;
        setUpPeriodicSaves();
    }

    private void setUpPeriodicSaves() {
        PermanentStateClass obj = this;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                obj.savePermanentState();
            }
        }, SAVE_PERIOD, SAVE_PERIOD);
    }

    synchronized void savePermanentState() {
        try {
            FileOutputStream out = new FileOutputStream(savePath);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(this);
            oos.close();
        } catch (IOException e) {
            Log.logError("Couldn't save permanent state");
            e.printStackTrace();
        }
    }

}
