package channels;

import service.Peer;
import utils.Log;

public class MDBChannel extends Channel {
    public MDBChannel(Peer parentPeer, String mcastAddr, String mcastPort) {
        super(parentPeer, mcastAddr, mcastPort);
        Log.logWarning("Backup channel initialized!");
    }
}
