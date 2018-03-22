package filesystem;

import java.util.concurrent.ConcurrentHashMap;

public class SystemManager {

    private ConcurrentHashMap<String, FileManager> storage;

    public SystemManager(ConcurrentHashMap<String, FileManager> storage) {
        this.storage = storage;
    }

}
