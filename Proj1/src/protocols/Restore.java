package protocols;

import channels.Channel;
import filesystem.Database;
import network.Message;
import service.Peer;
import utils.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static protocols.ProtocolSettings.ENHANCEMENT_RESTORE;
import static protocols.ProtocolSettings.isCompatibleWithEnhancement;

public class Restore implements Runnable, PeerData.MessageObserver {

    private Peer parentPeer;
    private Message request;
    private Database database;
    private Random random;
    private Future handler = null;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        this.database = parentPeer.getDatabase();
        this.random = new Random();

        Log.logWarning("Starting restore!");
    }

    @Override
    public void run() {
        //Ignore Chunks of own files
        if (request.getSenderID() == parentPeer.getID()) {
            Log.logWarning("Ignoring CHUNKs of own files");
            return;
        }

        String fileID = request.getFileID();
        int chunkNo = request.getChunkNo();

        //Access database to get the Chunk
        if (!database.hasChunk(fileID, chunkNo)) {
            Log.logError("Chunk not found locally: " + fileID + "/" + chunkNo);
            return;
        }

        byte[] chunkData = parentPeer.loadChunk(fileID, chunkNo);

        if (isCompatibleWithEnhancement(ENHANCEMENT_RESTORE, request, parentPeer)) {
            sendMessageToTCP(request, chunkData);
            sendMessageToMDR(request, null);
        } else {
            sendMessageToMDR(request, chunkData);
        }

        Log.logWarning("Finished restore!");
    }

    private Message createMessage(Message request, byte[] chunkData) {
        String[] args = {
                parentPeer.getVersion(),
                Integer.toString(parentPeer.getID()),
                request.getFileID(),
                Integer.toString(request.getChunkNo())
        };

        return new Message(Message.MessageType.CHUNK, args, chunkData);
    }

    private void sendMessageToTCP(Message request, byte[] chunkData) {
        Message msgToSend = createMessage(request, chunkData);

        String hostName = request.getTCPHost();
        int portNumber = request.getTCPPort();

        Socket serverSocket;

        try {
            serverSocket = new Socket(hostName, portNumber);
            Log.log("Connected to TCPServer");
            ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
            oos.writeObject(msgToSend);
            oos.close();
            serverSocket.close();
        } catch (IOException e) {
            Log.logError("Couldn't send CHUNK via TCP");
        }

        Log.logWarning("S TCP: " + request.toString());
    }

    private void sendMessageToMDR(Message request, byte[] chunkData) {
        Message msgToSend = createMessage(request, chunkData);

        parentPeer.getPeerData().attachChunkObserver(this);
        this.handler = parentPeer.sendDelayedMessage(
                Channel.ChannelType.MDR,
                msgToSend,
                random.nextInt(ProtocolSettings.MAX_DELAY),
                TimeUnit.MILLISECONDS
        );

        try {
            this.handler.wait();
            parentPeer.getPeerData().detachChunkObserver(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void update(Message msg) {
        if (this.handler == null)
            return;
        if (msg.getFileID().equals(request.getFileID()) && msg.getChunkNo() == request.getChunkNo()) {
            this.handler.cancel(true);
            Log.log("Cancelled CHUNK message, to avoid flooding host");
        }
    }
}
