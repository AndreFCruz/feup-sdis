package network;

import utils.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class MessageHandler implements Runnable {

    private BlockingQueue<Message> msgQueue;

    MessageHandler() {
        msgQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public void run() {
        Message msg;

        while (true) {
            try { // BlockingQueue.take() yields CPU until a message is available
                msg = msgQueue.take();
                dispatchMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Template Method
    protected abstract void dispatchMessage(Message msg);

    public void pushMessage(byte[] data, int length) {
        Message msgParsed; // create and parse the message
        try {
            msgParsed = new Message(data, length);
        } catch (Exception e) {
            Log.logError(e.getMessage());
            return;
        }

        msgQueue.add(msgParsed);
    }

}
