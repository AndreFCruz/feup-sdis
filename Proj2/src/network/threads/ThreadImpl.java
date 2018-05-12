package network.threads;

public abstract class HelperThread extends Thread {

    /**
     * Variable indicating whether the current thread should live.
     */
    private boolean alive = true;

    /**
     * Initialize current thread's resources.
     */
    protected abstract void initialize();

    /**
     * Gracefully terminate current thread.
     */
    protected abstract void terminate();

    /**
     * Action to be repeated whilst thread lives.
     */
    protected abstract void act();

}
