package io.gameq.gameqwindows.Structs;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class PacketTimer {

    private int key;
    private double time;

    public PacketTimer(int key, double time) {
        this.key = key;
        this.time = time;
    }

    public int getKey() {
        return key;
    }

    public double getTime() {
        return time;
    }
}
