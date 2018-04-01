package channels;

import service.Peer;
import utils.Log;

public class MDRChannel extends Channel {
    public MDRChannel(Peer parentPeer, String mcastAddr, String mcastPort) {
        super(parentPeer, mcastAddr, mcastPort);
        Log.logWarning("Restore channel initializated!");
    }
}
