package protocols.initiators.helpers;

import filesystem.Chunk;
import filesystem.ChunkInfo;
import service.Peer;

public class RemovedChunkHelper extends BackupChunkHelper {
    private ChunkInfo chunkInfo;

    public RemovedChunkHelper(Peer parentPeer, ChunkInfo chunkInfo, byte[] chunkData) {
        super(parentPeer, new Chunk(chunkInfo, chunkData));

        this.chunkInfo = chunkInfo;
    }

    @Override
    protected boolean isDesiredReplicationDegree() {
        return chunkInfo.getNumMirrors() >= chunkInfo.getReplicationDegree();
    }
}
