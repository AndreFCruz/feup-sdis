package protocols.initiators.helpers;

import channels.Channel;
import filesystem.Chunk;
import network.Message;
import protocols.ProtocolSettings;
import protocols.initiators.BackupInitiator;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class BackupChunkHelper implements Runnable {

    private final String protocolVersion;
    private Peer parentPeer;
    private Chunk chunk;
    private AtomicIntegerArray chunkReplication;

    public BackupChunkHelper(BackupInitiator backupInitiator, Chunk chunk) {
        this.chunk = chunk;
        this.parentPeer = backupInitiator.getParentPeer();
        this.protocolVersion = backupInitiator.getProtocolVersion();
        this.chunkReplication = parentPeer.getPeerData().getChunkReplication(chunk.getFileID());
    }

    BackupChunkHelper(Peer parentPeer, Chunk chunk) {
        this.chunk = chunk;
        this.parentPeer = parentPeer;
        this.protocolVersion = Peer.PROTOCOL_VERSION;
        this.chunkReplication = null;
    }

    @Override
    public void run() {

        int waitTime = 1000; // wait time, in milliseconds
        Message msg = generatePutChunkMsg(chunk, protocolVersion);

        Thread currentThread = Thread.currentThread();
        for (int i = 0; i < ProtocolSettings.PUTCHUNK_RETRIES; i++) {
            Log.log(currentThread.getId() + " i: " + i);
            if (isDesiredReplicationDegree()) {
                Log.logWarning("Achieved desired replication at i=" + i);
                break;
            }

            try {
                parentPeer.sendMessage(Channel.ChannelType.MDB, msg);
            } catch (IOException e) {
                Log.logError(e.getMessage());
            }

            sleep(waitTime);
            waitTime *= 2;
        }
    }

    protected boolean isDesiredReplicationDegree() {
        Log.log("Current perceived replication: " + chunkReplication.get(chunk.getChunkNo()));
        return chunkReplication != null && chunkReplication.get(chunk.getChunkNo()) >= chunk.getReplicationDegree();
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Message generatePutChunkMsg(Chunk chunk, String protocolVersion) {
        String[] args = {
                protocolVersion,
                Integer.toString(parentPeer.getID()),
                chunk.getFileID(),
                Integer.toString(chunk.getChunkNo()),
                Integer.toString(chunk.getReplicationDegree())
        };

        return new Message(Message.MessageType.PUTCHUNK, args, chunk.getData());
    }

}
