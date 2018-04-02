package channels;

import service.Peer;
import utils.Log;

public class MChannel extends Channel {
    public MChannel(Peer parentPeer, String mcastAddr, String mcastPort) {
        super(parentPeer, mcastAddr, mcastPort);
        Log.log("Control channel initialized!");
    }
}
