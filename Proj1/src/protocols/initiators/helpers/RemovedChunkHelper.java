package protocols.initiators.helpers;

import filesystem.ChunkData;
import filesystem.ChunkInfo;
import service.Peer;

public class RemovedChunkHelper extends BackupChunkHelper {
    private ChunkInfo chunkInfo;

    public RemovedChunkHelper(Peer parentPeer, ChunkInfo chunkInfo, byte[] chunkData) {
        super(parentPeer, new ChunkData(chunkInfo, chunkData));

        this.chunkInfo = chunkInfo;
    }

    @Override
    protected boolean isDesiredReplicationDegree() {
        return chunkInfo.getNumMirrors() >= chunkInfo.getReplicationDegree();
    }
}
