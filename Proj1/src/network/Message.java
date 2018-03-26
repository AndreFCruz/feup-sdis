package network;

import utils.Utils;

import java.io.*;

public class Message {

    private int numberArgs;
    //    Header
    private Utils.MessageType type;
    private String version;
    private int senderID;
    private String fileID;
    private int chunkNo = -1;
    private int replicationDegree = -1;
    //    Body
    private byte[] body;


    public Message(String msg) {
        parseMessage(msg);
    }

    public Message(byte[] data, int length) throws IOException {
        String header = extractHeader(data);

        String headerCleaned = header.trim().replaceAll("\\s+", " ");
        String[] headerSplit = headerCleaned.split("\\s+");

        parseHeader(headerSplit);

        if (type == Utils.MessageType.PUTCHUNK) {
            this.body = extractBody(data, header.length(), length);
        }

    }

    public Message(Utils.MessageType type, String[] args) {
        this.type = type;
        version = args[0];
        senderID = Integer.parseInt(args[1]);
        fileID = args[2];
        chunkNo = Integer.parseInt(args[3]);

        switch (this.type) {
            case PUTCHUNK:
                replicationDegree = Integer.parseInt(args[4]);
                break;
            case STORED:
                break;
            default:
                break;
        }
    }

    public Message(Utils.MessageType type, String[] args, byte[] data) {
        this.type = type;
        version = args[0];
        senderID = Integer.parseInt(args[1]);
        fileID = args[2];
        chunkNo = Integer.parseInt(args[3]);

        switch (this.type) {
            case PUTCHUNK:
                replicationDegree = Integer.parseInt(args[4]);
                body = data;
                break;
            case STORED:
                break;
            default:
                break;
        }
    }

    private String extractHeader(byte[] data) {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));

        String header = "";

        try {
            header = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();

        }

        System.out.println(header + "|");
        return header;
    }

    private byte[] extractBody(byte[] data, int headerLength, int dataLength) throws IOException {
        int length = dataLength;
        int readBytes = length - headerLength - 4;
        ByteArrayInputStream message = new ByteArrayInputStream(data, headerLength + 4, readBytes);


        byte[] bodyContent = new byte[readBytes];

        message.read(bodyContent, 0, readBytes);
        System.out.println("data:" + dataLength);
        System.out.println("headerLength:" + headerLength);
        System.out.println("readBytes: " + readBytes);
        return bodyContent;
    }

    private void parseMessage(String msg) {
        //Split header from body ( \R -> CRLF)
        String[] msgSplit = msg.split("\\R\\R", 2);
        System.out.println("c3: " + msg.length());
        String header, body = null;

        if (msgSplit.length == 0 || msgSplit.length > 2)
            return; //message discarded
        else if (msgSplit.length == 2)
            body = msgSplit[1];

        header = msgSplit[0];

        System.out.println("header: " + header.length());

        String headerCleaned = header.trim().replaceAll("\\s+", " ");
        String[] headerSplit = headerCleaned.split("\\s+");

        parseHeader(headerSplit);

        if (body != null) {
            System.out.println("body: " + body.length());
            this.body = body.getBytes();
            System.out.println("parser:" + this.body.length);
        }
    }

    private void parseHeader(String[] headerSplit) {

        switch (headerSplit[0]) {
            case "PUTCHUNK":
                type = Utils.MessageType.PUTCHUNK;
                numberArgs = 6;
                break;
            case "STORED":
                type = Utils.MessageType.STORED;
                numberArgs = 5;
                break;
            default:
                return;
        }

        if (headerSplit.length != numberArgs)
            return;

        version = headerSplit[1];

        senderID = Integer.parseInt(headerSplit[2]);

        if (numberArgs >= 4)
            fileID = headerSplit[3];

        chunkNo = Integer.parseInt(headerSplit[4]);

        if (type == Utils.MessageType.PUTCHUNK) {
            replicationDegree = Integer.parseInt(headerSplit[5]);
        }


    }

    public Utils.MessageType getType() {
        return type;
    }

    private String getMsgAsString() {
        if (body != null) {
            String bodyStr = new String(body);
            return getHeaderAsString() + bodyStr;
        } else
            return getHeaderAsString();
    }

    public String getHeaderAsString() {
        String str = "";

        switch (type) {
            case PUTCHUNK:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + replicationDegree + " " + Utils.CRLF + Utils.CRLF;
                break;
            case STORED:
                str = type + " " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
                break;
            default:
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

}
