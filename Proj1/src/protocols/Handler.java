package protocols;

import network.Message;

public class Handler implements Runnable {

    public Handler(){
        //Executor Service aka threads(each for each protocol)
        //Queue

    }


    @Override
    public void run() {
        try{
            //Parse messages from channels
            //Handle result

        }catch (){

        }
    }

    private void dispatchMessage(Message msg){
        //see type of message and
        //switch case
        //chunk backup, chunk restore, file deletion, space reclamming, putchunk, getchunk
    }

    private void pushMessage(String msg){
        Message msgParsed = new Message(msg); //create and parse the message
        //add to queue
    }

}
