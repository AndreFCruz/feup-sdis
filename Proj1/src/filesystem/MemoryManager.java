package filesystem;

import utils.Log;

public class MemoryManager extends PermanentStateClass {
    private static final long serialVersionUID = 2L;

    private long maxMemory;
    private long usedMemory;

    MemoryManager(long maxMemory, String savePath) {
        this.maxMemory = maxMemory;
        this.usedMemory = 0;

        this.setUp(savePath);
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getUsedMemory() {
        return this.usedMemory;
    }

    public long getAvailableMemory() {
        return maxMemory - usedMemory;
    }

    public void reduceUsedMemory(long n) {
        usedMemory -= n;
        if (usedMemory < 0) {
            usedMemory = 0;
            Log.logError("Used memory went below 0");
        }
    }

    public boolean increaseUsedMemory(long n) {
        if (usedMemory + n > maxMemory) {
            Log.logWarning("Tried to surpass memory restrictions");
            return false;
        }
        usedMemory += n;
        Log.logWarning("Used memory: " + usedMemory + " / " + maxMemory);
        return true;
    }

}
