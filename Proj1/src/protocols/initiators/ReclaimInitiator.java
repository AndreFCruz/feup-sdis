package protocols.initiators;

import filesystem.SystemManager;
import service.Peer;

public class ReclaimInitiator implements Runnable {

    private Peer parentPeer;
    private SystemManager systemManager;

    public ReclaimInitiator(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.systemManager = parentPeer.getSystemManager();
    }

    @Override
    public void run() {
        while (systemManager.getAvailableMemory() < 0) {
            // TODO get a random chunk
        }
    }
}
