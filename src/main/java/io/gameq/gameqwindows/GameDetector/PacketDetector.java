package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Structs.Packet;
import io.gameq.gameqwindows.Structs.Status;

import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public abstract class PacketDetector extends GameDetector{

    LinkedList<Packet> packetQueue = new LinkedList<Packet>();
    int queueMaxSize = -1;
    boolean isCapturing = false;
    PacketParser packetParser = PacketParser.getInstance();

    protected void handle(Packet newPacket) {
        if(newPacket.getPacketLength() > 5){
            System.out.println("src: " + newPacket.getSrcPort() + " dst: " + newPacket.getDstPort() + " len: " +
                    newPacket.getPacketLength() + " time: " +  newPacket.getCaptureTime()/1000.0);

            if(getStatus() != Status.InGame){
                packetQueue.addFirst(newPacket);
                if(packetQueue.size() >= queueMaxSize) {
                    packetQueue.removeLast();
                }
            }
            update(newPacket);
        }
    }

    protected void handleTest(Packet newPacket) {
        System.out.println("src: " + newPacket.getSrcPort() + " dst: " + newPacket.getDstPort() + " len: " +
                newPacket.getPacketLength() + " time: " +   newPacket.getCaptureTime()/1000.0);
        update(newPacket);
    }

    protected abstract void update(Packet newPacket);

    @Override
    public String fileToString(){
        String log = "";
        for(Packet p: packetQueue){
            log = log + p.getSrcPort() + "," + p.getDstPort() + "," + p.getPacketLength() + "," + p.getCaptureTime();
        }
        return log;
    }

    @Override
    public void stopDetection(){
        PacketParser.getInstance().terminate();
        super.stopDetection();
    }

    @Override
     public void saveDetection(){
        super.saveDetection();
        packetQueue = new LinkedList<>();
    }

    @Override
    public void saveMissedDetection(){
        super.saveMissedDetection();
        packetQueue = new LinkedList<>();
    }
}
