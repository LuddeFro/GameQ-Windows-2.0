package io.gameq.gameqwindows.Structs;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class PacketTimer {

    private int key;
    private long time;

    public PacketTimer(int key, long time) {
        this.key = key;
        this.time = time;
    }

    public int getKey() {
        return key;
    }

    public long getTime() {
        return time;
    }
}
