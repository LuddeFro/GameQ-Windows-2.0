package io.gameq.gameqwindows;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class Packet {

    private int dstPort;
    private int srcPort;
    private int packetLength;
    private long captureTime;

    public Packet(int dstPort, int srcPort, int packetLength, long captureTime){

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

    public long getCaptureTime() {
        return captureTime;
    }
}
