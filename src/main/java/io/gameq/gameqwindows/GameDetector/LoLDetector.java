package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by fabianwikstrom on 8/13/2015.
 */
public class LoLDetector extends PacketDetector {

    private String lolFilter = "tcp src port 2099 or tcp src port 5223 or tcp src port 5222 or tcp dst port 2099 or tcp dst port 5223 or tcp dst port 5222";
    private Set<Integer> ports = new HashSet<Integer>(Arrays.asList(2099, 5223, 5222));
    private int queuePort = -1;

    private LinkedList<PacketTimer> srcQTimer = new LinkedList<>();
    private PacketMap srcQCounter = new PacketMap(new int[]{300, 400, 500, 600, 700, 800, 900, 1100});

    private LinkedList<PacketTimer> dstQTimer = new LinkedList<>();
    private PacketMap dstQCounter = new PacketMap(new int[]{100, 400, 500, 700, 800, 900});

    private LinkedList<PacketTimer> stopQTimer = new LinkedList<>();
    private PacketMap stopQCounter = new PacketMap(new int[]{100, 300, 800, 900, 1100});

    private LinkedList<PacketTimer> stopDstQTimer = new LinkedList<>();
    private PacketMap stopDstQCounter = new PacketMap(new int[]{100, 300, 700, 800, 900});

    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<>();
    private PacketMap packetCounter1 = new PacketMap(new int[]{1300});

    private LinkedList<PacketTimer> spamDetector = new LinkedList<>();

    private double queueStartTime = -1;

    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.LoL);
        setCountDownLength(10);
        updateStatus(Status.InLobby);

        if(!isCapturing){
            PacketParser.getInstance().start(lolFilter, this);
            isCapturing = true;
        }
    }

    @Override
    public void resetDetection(){
        super.resetDetection();
        resetQueueTimer();
        resetGameTimer();
    }

    public void resetQueueTimer(){

        spamDetector = new LinkedList<>();

        srcQTimer = new LinkedList<>();
        srcQCounter =  new PacketMap(new int[]{300, 400, 500, 600, 700, 800, 900, 1100});

        queuePort = -1;

        dstQTimer =  new LinkedList<>();
        dstQCounter =  new PacketMap(new int[]{100, 400, 500, 700, 800, 900});

        stopQTimer =  new LinkedList<>();
        stopQCounter = new PacketMap(new int[]{100, 300, 800, 900, 1100});

        stopDstQTimer =  new LinkedList<>();
        stopDstQCounter = new PacketMap(new int[]{100, 300, 700, 800, 900});
    }

    public void resetGameTimer(){
        gameTimer1 = new LinkedList<>();
        packetCounter1 = new PacketMap(new int[]{1300});
        queueStartTime = -1;
    }

    @Override
    protected void update(Packet newPacket) {

        //IN LOBBY
        if(getStatus() == Status.InLobby){
            boolean queueing = isQueueing(newPacket);
            if(queueing){updateStatus(Status.InQueue);
                resetQueueTimer();}
        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){
            boolean gameReady = isGameReady(newPacket);
            boolean stoppedQueue = stoppedQueueing(newPacket);

            if(gameReady){updateStatus(Status.GameReady);}
            else if(stoppedQueue){updateStatus(Status.InLobby);
                resetQueueTimer();}
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
        }

        //IN GAME
        else  if(getStatus() == Status.InGame){
        }

        else {
        }
    }


    private boolean isQueueing(Packet p){

        while(!spamDetector.isEmpty() && p.getCaptureTime() - spamDetector.getLast().getTime() > 1.0){
            System.out.println(p.getCaptureTime() - spamDetector.getLast().getTime());
            spamDetector.removeLast();
        }

        spamDetector.addFirst(new PacketTimer(p.getPacketLength(), p.getCaptureTime()));

        while(!srcQTimer.isEmpty() && p.getCaptureTime() - srcQTimer.getLast().getTime() > 2.0){
            int key = srcQTimer.removeLast().getKey();
            srcQCounter.put(key, srcQCounter.get(key) - 1);
        }

        while(!dstQTimer.isEmpty() && p.getCaptureTime() - dstQTimer.getLast().getTime() > 2.0){
            int key = dstQTimer.removeLast().getKey();
            dstQCounter.put(key, dstQCounter.get(key) -1);
        }


        for (int key : srcQCounter.keySet()){
            if((p.getPacketLength() <= key + 99 && p.getPacketLength() >= key  && ports.contains(p.getSrcPort()))){
                if((p.getPacketLength() >= 600 && p.getSrcPort() == 5223) || (p.getPacketLength() < 600 && p.getSrcPort() == 2099)) {
                    srcQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                    srcQCounter.put(key, srcQCounter.get(key) + 1);
                }
            }
        }

        for (int key : dstQCounter.keySet()) {
            if ((p.getPacketLength() <= key + 99 && p.getPacketLength() >= key && ports.contains(p.getDstPort()))) {
                if ((p.getPacketLength() >= 600 && p.getDstPort() == 5223) || (p.getPacketLength() < 600 && p.getDstPort() == 2099)) {
                    dstQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                    dstQCounter.put(key, dstQCounter.get(key) + 1);
                }
            }
        }

        System.out.println(spamDetector.size());

        if(spamDetector.size() > 10){return false;}
        else if((srcQCounter.get(400) > 0 || srcQCounter.get(800) > 0 || srcQCounter.get(700) > 0) && (dstQCounter.get
                (500) > 0 ||
                dstQCounter.get(800)  > 0 || dstQCounter.get(700)  > 0 || dstQCounter.get(400)  > 0) && (srcQTimer.size() >= 2 &&
                dstQTimer.size() >= 2) && (srcQTimer.size() + dstQTimer.size() >= 3)){
            queueStartTime = p.getCaptureTime();
            return true;
        }
        else if((srcQCounter.get(300) > 0 || srcQCounter.get(400) > 0) && (dstQCounter.get(500) > 0 ||
                dstQCounter.get(100) > 0) && (srcQTimer.size() >= 3 && dstQTimer.size() >= 3)){
            queueStartTime = p.getCaptureTime();
            return true;}
        else if((srcQCounter.get(400) > 0 && dstQCounter.get(400) > 0 && srcQCounter.get(700) > 0 && dstQCounter.get
                (700) > 0)){return true;}
        else{return false;}
    }


    private boolean stoppedQueueing(Packet p) {

        while(!stopQTimer.isEmpty() && p.getCaptureTime() - stopQTimer.getLast().getTime() > 2.0){
            int key = stopQTimer.removeLast().getKey();
            stopQCounter.put(key, stopQCounter.get(key) -1);
        }

        while(!stopDstQTimer.isEmpty() && p.getCaptureTime() - stopDstQTimer.getLast().getTime() > 2.0){
            int key = stopDstQTimer.removeLast().getKey();
            stopDstQCounter.put(key, stopDstQCounter.get(key) -1);
        }

        for (int key : stopQCounter.keySet()){
            if((p.getPacketLength() <= key + 99 && p.getPacketLength() >= key && ports.contains(p.getSrcPort()))){
                stopQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                stopQCounter.put(key, stopQCounter.get(key) + 1);
            }
        }

        for (int key : stopDstQCounter.keySet()){
            if((p.getPacketLength() <= key + 99 && p.getPacketLength() >= key && ports.contains(p.getDstPort()))){
                stopDstQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                stopDstQCounter.put(key, stopDstQCounter.get(key) + 1);
            }
        }

        if((stopQCounter.get(100) > 0 || (stopQCounter.get(900) > 0 || stopQCounter.get(800) > 0)) &&
                (stopDstQCounter.get(300) > 0 ||
                stopDstQCounter.get(800) > 0) && (stopQTimer.size() >= 2 && stopDstQTimer.size() >= 2))
        {return true;}
        else if((stopQCounter.get(900) > 0 || stopQCounter.get(1100) > 0 || stopQCounter.get(300) > 0) &&
                (stopDstQCounter.get(100) > 0 || stopDstQCounter.get(300) > 0) && (stopQTimer.size() >= 3 &&
                stopDstQTimer.size() >= 3))
        {return true;}
        else if(stopQCounter.get(100) > 0 && (stopQCounter.get(900) + stopQCounter.get(800) > 0)
                && stopDstQCounter.get(300) > 0 && (stopDstQCounter.get(700) + stopDstQCounter.get(800) +
            stopDstQCounter.get(900) > 0)){return true;}
        else{return false;}
    }


    private boolean isGameReady(Packet p) {

        while(!gameTimer1.isEmpty() && p.getCaptureTime() - gameTimer1.getLast().getTime() > 3.0){
            int key = gameTimer1.removeLast().getKey();
            packetCounter1.put(key, packetCounter1.get(key) - 1);
        }

        for (int key: packetCounter1.keySet()){
            if(p.getPacketLength() <= key + 99 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort == -1)
                    && ports.contains(p.getSrcPort()) && (p.getCaptureTime() - queueStartTime > 0.3)){
                gameTimer1.addFirst(new PacketTimer(key, p.getCaptureTime()));
                packetCounter1.put(key, packetCounter1.get(key) + 1);
            }
        }

        if(packetCounter1.get(1300) >= 2)
        {return true;}
        else{return false;}
    }
}
