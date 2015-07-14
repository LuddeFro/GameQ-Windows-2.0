package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Structs.Packet;

import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public interface PacketDetector{

    LinkedList<Packet> packetQueue = null;
    int queueMaxSize = -1;
    boolean isCapturing = false;
    // static var packetParser:PacketParser {get set} ???

    public void handle(Packet newPacket);

    public void handleTest(Packet newPacket);

    public void update(Packet newPacket);
}
