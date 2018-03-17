package filesystem;

import java.util.concurrent.ConcurrentHashMap;

public class SystemManager {

    private ConcurrentHashMap<String, File> storage;

    public SystemManager(ConcurrentHashMap<String, File> storage) {
        this.storage = storage;
    }
    
}
