package protocols;

import network.Message;
import service.Peer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Handler implements Runnable {
    private Peer parentPeer;
    private LinkedBlockingQueue<Message> msgQueue;
    private ExecutorService executor;

    public Handler(Peer parentPeer) {
        this.parentPeer = parentPeer;
        msgQueue = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool(3);

        //Executor Service aka threads(each for each protocol)
        //Queue

    }


    @Override
    public void run() {
        Message msg;

        //probably will terminate when the queue is empty...
        while (true) {
            while ((msg = msgQueue.poll()) != null) {
                dispatchMessage(msg);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //            //Parse messages from channels
//            //Handle result

    }

    private void dispatchMessage(Message msg) {
        //see type of message and
        //switch case
        //chunk backup, chunk restore, file deletion, space reclamming, putchunk, getchunk
//        System.out.println("tu 2");
//        System.out.println(msg);

        switch (msg.getType()) {
            case PUTCHUNK:
                Backup backup = new Backup(parentPeer, msg);
                executor.execute(backup);
                break;
            case STORED:
                System.out.println("Stored received");
                //           parentPeer.updateFileStorage(message);
                break;

            default:
                return;

        }

    }

    public void pushMessage(String msg) {
        Message msgParsed = new Message(msg); //create and parse the message
        msgQueue.add(msgParsed);
        System.out.println(msgParsed.getHeaderAsString());
//        System.out.println("eu 1");
//        System.out.println(msg);
        //add to queue
    }


    public void pushMessage(byte[] data, int length) throws IOException {
        Message msgParsed = new Message(data, length); //create and parse the message
        msgQueue.add(msgParsed);
    }
}