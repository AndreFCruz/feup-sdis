package network;

public class Message {

//    Header
    private String type;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo = -1;
    private int replicationDegree = -1;
//    Body

    private byte[] body;

    public Message(String msg) {
        parseMessage(msg);

    }

    private void parseMessage(String msg) {
        //Parse message
    }

}
