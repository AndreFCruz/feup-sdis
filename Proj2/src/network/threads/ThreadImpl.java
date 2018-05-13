package network.threads;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ThreadImpl extends Thread {

    /**
     * Variable indicating whether the current thread should live.
     */
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Action to be repeated whilst thread lives.
     * Function should block if no action is yet to be made.
     */
    protected abstract void act();

    /**
     * Gracefully terminate current thread.
     */
    protected abstract void terminate();

    @Override
    public void run() {
        while(running.get()) {
            this.act();
        }

        this.terminate();
    }

    /**
     * Sets this thread to gracefully terminate.
     */
    public void toDie() {
        this.running.set(false);
    }
}
