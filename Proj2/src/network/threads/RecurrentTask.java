package network.threads;

import java.util.Timer;
import java.util.TimerTask;

public abstract class RecurrentTask extends Thread {

    private final int INTERVAL;

    private Timer timer;

    RecurrentTask(int interval) {
        this.INTERVAL = interval;
        this.timer = new Timer();
    }

    @Override
    public synchronized void start() {
        Runnable runnable = this;

        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                },
                INTERVAL,
                INTERVAL
        );
    }

    public void terminate() {
        timer.cancel();
    }

}
