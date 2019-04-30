package ir.markazandroid.advertiser.network;

/**
 * Coded by Ali on 17/04/2017.
 */
public class State {

    public static final int NOT_YET = 0;
    public static final int FAILED = 1;
    public static final int IS_RUNNING = 2;
    public static final int DONE = 3;
    public int state;

    public State(int state) {
        this.state = state;
    }
}
