package network;

import utils.Log;
import utils.Utils;

import java.awt.*;
import java.io.*;

import static protocols.ProtocolSettings.TCPSERVER_PORT;
import static utils.Utils.getIPV4Address;

public class Message implements Serializable{

    private int numberArgs;
    //    Header
    private MessageType type;
    private String version;
    private int senderID;
    private String fileID;
    private int chunkNo;
    private int replicationDegree;
    //    Body
    private byte[] body;
    private String mTCPHost;
    private int mTCPPort;

    //Constructor that handle received messages
    public Message(byte[] data, int length) throws Exception {
        String header = extractHeader(data);

        if (header.equals("") || !parseHeader(header)) {
            throw new Exception("Invalid message...Ignoring it!");
        }

        if (type == MessageType.PUTCHUNK || type == MessageType.CHUNK) {
            this.body = extractBody(data, header.length(), length);
        }
    }

    //Constructor that handle send messages without body
    public Message(MessageType type, String[] args) {
        this.type = type;
        version = args[0];
        senderID = Integer.parseInt(args[1]);

        if(type == MessageType.UP)
            return;

        fileID = args[2];

        if (type != MessageType.DELETE && type != MessageType.DELETED)
            chunkNo = Integer.parseInt(args[3]);

        if (type == MessageType.PUTCHUNK) {
            replicationDegree = Integer.parseInt(args[4]);
        }

        if(type == MessageType.ENH_GETCHUNK){
            mTCPPort = Integer.parseInt(args[4]);
            mTCPHost = getIPV4Address();
        }
    }

    //Constructor that handle send messages with body
    public Message(MessageType type, String[] args, byte[] data) {
        this(type, args);
        body = data;
    }

    private String extractHeader(byte[] data) {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));

        String header = "";

        try {
            header = reader.readLine(); //TODO: Check if have two CRLF
        } catch (IOException e) {
            e.printStackTrace();
        }

        return header;
    }

    private byte[] extractBody(byte[] data, int headerLength, int dataLength) {
        int length = dataLength;
        int readBytes = length - headerLength - 4;
        ByteArrayInputStream message = new ByteArrayInputStream(data, headerLength + 4, readBytes);
        byte[] bodyContent = new byte[readBytes];

        message.read(bodyContent, 0, readBytes);

        return bodyContent;
    }

    private boolean parseHeader(String header) {

        String headerCleaned = header.trim().replaceAll("\\s+", " "); //More than one space between fields
        String[] headerSplit = headerCleaned.split("\\s+"); //Split by space the header elements

        switch (headerSplit[0]) {
            case "PUTCHUNK":
                type = MessageType.PUTCHUNK;
                numberArgs = 6;
                break;
            case "STORED":
                type = MessageType.STORED;
                numberArgs = 5;
                break;
            case "GETCHUNK":
                type = MessageType.GETCHUNK;
                numberArgs = 5;
                break;
            case "CHUNK":
                type = MessageType.CHUNK;
                numberArgs = 5;
                break;
            case "DELETE":
                type = MessageType.DELETE;
                numberArgs = 4;
                break;
            case "DELETED":
                type = MessageType.DELETED;
                numberArgs = 4;
                break;
            case "REMOVED":
                type = MessageType.REMOVED;
                numberArgs = 5;
                break;
            case "ENH_GETCHUNK":
                type = MessageType.ENH_GETCHUNK;
                numberArgs = 6;
                break;
            case "UP":
                type = MessageType.UP;
                numberArgs = 3;
                break;
            default:
                return false;
        }

        if (headerSplit.length != numberArgs)
            return false;

        version = headerSplit[1];
        senderID = Integer.parseInt(headerSplit[2]);

        if(type == MessageType.UP)
            return true;

        fileID = headerSplit[3];

        if (numberArgs > 4)
            chunkNo = Integer.parseInt(headerSplit[4]);

        if (type == MessageType.PUTCHUNK)
            replicationDegree = Integer.parseInt(headerSplit[5]);

        if(type == MessageType.ENH_GETCHUNK){
            String[] tcpAddress = headerSplit[5].split(":");
            mTCPHost = tcpAddress[0];
            mTCPPort = Integer.parseInt(tcpAddress[1]);
        }

        return true;
    }

    public MessageType getType() {
        return type;
    }

    public String getHeaderAsString() {
        String str;

        switch (type) {
            case PUTCHUNK:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + replicationDegree + " " + Utils.CRLF + Utils.CRLF;
                break;
            case DELETE:
            case DELETED:
                str = type + " " + version + " " + senderID + " " + fileID + " " + Utils.CRLF + Utils.CRLF;
                break;
            case ENH_GETCHUNK:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " +
                        mTCPHost + ":" + mTCPPort + " " + Utils.CRLF + Utils.CRLF;
                break;
            case UP:
                str = type + " " + version + " " + senderID + " " + Utils.CRLF + Utils.CRLF;
                break;
            default:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
                break;
        }

        return str;
    }

    public byte[] getBytes() throws IOException {

        byte header[] = getHeaderAsString().getBytes();

        if (body != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(body);
            return outputStream.toByteArray();

        } else
            return header;
    }

    public String getVersion() {
        return version;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        String str;

        switch (type) {
            case PUTCHUNK:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo;
                break;
            case DELETE:
            case DELETED:
                str = type + " " + version + " " + senderID + " " + fileID;
                break;
            case ENH_GETCHUNK:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " +
                        mTCPHost + ":" + mTCPPort;
                break;
            case UP:
                str = type + " " + version + " " + senderID;
                break;
            default:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo;
                break;
        }

        return str;
    }

    public String getTCPHost() {
        return mTCPHost;
    }

    public int getTCPPort() {
        return mTCPPort;
    }

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        REMOVED,
        DELETE,
        ENH_GETCHUNK,
        DELETED,
        UP,
        CHUNK
    }
}