package protocols.initiators.helpers;

import channels.Channel;
import filesystem.ChunkData;
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
    private ChunkData chunk;
    private AtomicIntegerArray chunkReplication;

    public BackupChunkHelper(BackupInitiator backupInitiator, ChunkData chunk) {
        this.chunk = chunk;
        this.parentPeer = backupInitiator.getParentPeer();
        this.protocolVersion = backupInitiator.getProtocolVersion();
        this.chunkReplication = parentPeer.getPeerData().getChunkReplication(chunk.getFileID());
    }

    BackupChunkHelper(Peer parentPeer, ChunkData chunk) {
        this.chunk = chunk;
        this.parentPeer = parentPeer;
        this.protocolVersion = parentPeer.getVersion();
        this.chunkReplication = null;
    }

    @Override
    public void run() {

        int waitTime = 1000; // wait time, in milliseconds
        Message msg = generatePutChunkMsg(chunk, protocolVersion);

        for (int i = 0; i < ProtocolSettings.PUTCHUNK_RETRIES; i++) {
            if (isDesiredReplicationDegree()) {
                Log.log("Achieved desired replication at i=" + i);
                break;
            }

            try {
                parentPeer.sendMessage(Channel.ChannelType.MDB, msg);
            } catch (IOException e) {
                Log.logError("Couldn't send message to multicast channel!");
            }

            sleep(waitTime);
            waitTime *= 2;
        }
    }

    protected boolean isDesiredReplicationDegree() {
        Log.log("Current perceived replication of " + chunk.getChunkNo() + ": " + chunkReplication.get(chunk.getChunkNo()));
        return chunkReplication != null && chunkReplication.get(chunk.getChunkNo()) >= chunk.getReplicationDegree();
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Log.logError(e.getMessage());
        }
    }

    private Message generatePutChunkMsg(ChunkData chunk, String protocolVersion) {
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
