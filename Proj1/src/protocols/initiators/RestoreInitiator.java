package protocols.initiators;

import channels.Channel;
import filesystem.Chunk;
import filesystem.FileInfo;
import network.Message;
import protocols.initiators.helpers.TCPServer;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import static filesystem.SystemManager.fileMerge;
import static protocols.ProtocolSettings.*;

public class RestoreInitiator implements Runnable {

    private FileInfo fileInfo;
    private String filePath;
    private String version;

    private Peer parentPeer;
    private TCPServer tcpServer;

    public RestoreInitiator(String version, String filePath, Peer parentPeer) {
        this.version = version;
        this.filePath = filePath;
        this.parentPeer = parentPeer;
        fileInfo = parentPeer.getDatabase().getFileInfoByPath(filePath);

        Log.logWarning("Starting restoreInitiator!");
    }

    @Override
    public void run() {
        if (fileInfo == null) {
            Log.logError("File not found for RESTORE");
            return;
        }

        // Activate restore flag
        parentPeer.setRestoring(true, fileInfo.getFileID());

        //Start TCPServer if enhancement
        if (isPeerCompatibleWithEnhancement(ENHANCEMENT_RESTORE, parentPeer)) {
            initializeTCPServer();
        }

        //Log.logWarning("Sending GETCHUNK messages");
        // Send GETCHUNK to MC
        for (int i = 0; i < fileInfo.getNumChunks(); i++) {
            if (isPeerCompatibleWithEnhancement(ENHANCEMENT_RESTORE, parentPeer)) {
                sendMessageToMC(Message.MessageType.ENH_GETCHUNK, i);
            } else {
                sendMessageToMC(Message.MessageType.GETCHUNK, i);
            }
        }

        //Log.logWarning("Waiting for restored chunks");
        while (!parentPeer.hasRestoreFinished(filePath, fileInfo.getFileID())) {
            Thread.yield();
            // TODO sleep ?
            // Probably this will kill the cpu :')
        }

        if (isPeerCompatibleWithEnhancement(ENHANCEMENT_RESTORE, parentPeer)) {
            closeTCPServer();
        }

        Log.logWarning("Received all chunks");
        ConcurrentMap<Integer, Chunk> chunksRestored = parentPeer.getPeerData().getChunksRestored(fileInfo.getFileID());
        String pathToSave = parentPeer.getPath("restores");

        try {
            parentPeer.getSystemManager().saveFile(
                    fileInfo.getFileName(),
                    pathToSave,
                    fileMerge(new ArrayList<>(chunksRestored.values()))
            );
        } catch (IOException e) {
            e.printStackTrace();
            Log.logError("Failed saving file at " + fileInfo.getPath());
        }

        // File no longer restoring
        parentPeer.setRestoring(false, fileInfo.getFileID());
        Log.logWarning("Finished restoreInitiator!");
    }

    private void closeTCPServer() {
        tcpServer.closeTCPServer();
    }

    private void initializeTCPServer() {
        tcpServer = new TCPServer(parentPeer);
        new Thread(tcpServer).start();
    }


    private boolean sendMessageToMC(Message.MessageType type, int chunkNo) {
        String[] args = {
                version,
                Integer.toString(parentPeer.getID()),
                fileInfo.getFileID(),
                Integer.toString(chunkNo),
                Integer.toString(parentPeer.getID() + TCPSERVER_PORT)
        };

        Message msg = new Message(type, args);

        try {
            parentPeer.sendMessage(Channel.ChannelType.MC, msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
