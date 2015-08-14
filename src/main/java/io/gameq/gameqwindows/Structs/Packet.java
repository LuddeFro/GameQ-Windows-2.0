package io.gameq.gameqwindows.Structs;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class Packet {

    private int srcPort;
    private int dstPort;
    private int packetLength;
    private double captureTime;

    public Packet(int srcPort, int dstPort, int packetLength, double captureTime){

        this.dstPort = dstPort;
        this.srcPort = srcPort;
        this.packetLength = packetLength;
        this.captureTime = captureTime;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public double getCaptureTime() {
        return captureTime;
    }
}
