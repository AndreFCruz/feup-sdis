package channels;

import service.Peer;

public class MChannel extends Channel {
    public MChannel(Peer parentPeer, String mcastAddr, String mcastPort) {
        super(parentPeer, mcastAddr, mcastPort);
        System.out.println("Control channel initializated!");
    }
}
