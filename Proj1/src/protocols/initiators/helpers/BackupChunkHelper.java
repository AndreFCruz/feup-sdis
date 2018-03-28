package protocols.initiators.helpers;

import filesystem.Chunk;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class BackupChunkHelper implements Runnable {

    private Chunk chunk;
    private AtomicIntegerArray chunkReplication;

    BackupChunkHelper(Chunk chunk, AtomicIntegerArray chunkReplication) {
        this.chunk = chunk;
        this.chunkReplication = chunkReplication;
    }

    @Override
    public void run() {

    }
}
