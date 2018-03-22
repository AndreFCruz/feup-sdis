package channels;

import service.Peer;

public class MDRChannel extends Channel {
    public MDRChannel(Peer parentPeer, String mcastAddr, String mcastPort) {
        super(parentPeer, mcastAddr, mcastPort);
        System.out.println("Restore channel initializated!");
    }
}
