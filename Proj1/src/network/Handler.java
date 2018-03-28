package network;

import filesystem.Chunk;
import protocols.Backup;
import protocols.PeerData;
import protocols.Restore;
import service.Peer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Handler implements Runnable {
    private Peer parentPeer;
    private PeerData peerData;
    private BlockingQueue<Message> msgQueue;
    private ExecutorService executor;

    public Handler(Peer parentPeer) {
        this.parentPeer = parentPeer;
        this.peerData = parentPeer.getPeerData();
        msgQueue = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool(3);
    }


    @Override
    public void run() {
        Message msg;

        while (true) {
            try {
                msg = msgQueue.take();
                dispatchMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchMessage(Message msg) {
        if (msg == null) {
            System.err.println("Null Message Received");
            return;
        }

        System.out.println("R: " + msg.getHeaderAsString() + "|");
        switch (msg.getType()) {
            case PUTCHUNK:
                Backup backup = new Backup(parentPeer, msg);
                executor.execute(backup);
                break;
            case STORED:
                peerData.addChunkReplication(msg.getFileID(), msg.getChunkNo());
                break;
            case GETCHUNK:
                Restore restore = new Restore(parentPeer, msg);
                executor.execute(restore);
                break;
            case CHUNK:
                System.out.println("Chunk received");
                if (parentPeer.getFlagRestored(msg.getFileID())) {
                    parentPeer.addChunkToRestore(new Chunk(msg.getFileID(), msg.getChunkNo(), -1, msg.getBody()));
                } else {
                    System.out.println("Discard chunk");
                }
                break;
            default:
                return;

        }

    }

    public void pushMessage(byte[] data, int length) throws IOException {
        Message msgParsed = new Message(data, length); //create and parse the message
        msgQueue.add(msgParsed);
    }
}