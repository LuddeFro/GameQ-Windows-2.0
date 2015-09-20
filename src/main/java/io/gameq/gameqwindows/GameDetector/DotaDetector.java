package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.*;
import javafx.application.Application;

import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class DotaDetector extends PacketDetector {

    private String dotaFilter = "udp src portrange 27000-28999 or udp dst portrange 27000-28999";
    private LinkedList<Packet> packetQueue = new LinkedList<>();
    private int queueMaxSize = 200;
    private boolean isCapturing = false;

    private int portMin = 27000;
    private int portMax = 27050;

    private int queuePort = -1;
    private boolean isProbablyGame = false;

    private LinkedList<PacketTimer> srcQTimer = new LinkedList<>();
    private PacketMap srcQCounter = new PacketMap(new int[]{78,158,270,285});

    private LinkedList<PacketTimer> dstQTimer = new LinkedList<>();
    private PacketMap dstQCounter = new PacketMap(new int[]{78, 126, 174, 222});

    private LinkedList<PacketTimer> stopQTimer = new LinkedList<>();
    private PacketMap stopQCounter = new PacketMap(new int[]{78,250});

    private LinkedList<PacketTimer> stopDstQTimer = new LinkedList<>();
    private PacketMap stopDstQCounter = new PacketMap(new int[]{142, 174, 206, 250});

    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<>();
    private PacketMap packetCounter1 = new PacketMap(new int[]{600, 700, 800, 900, 1000, 1100, 1200, 1300});

    private LinkedList<PacketTimer> gameTimer2 = new LinkedList<>();
    private PacketMap packetCounter2 = new PacketMap(new int[]{164, 174, 190, 206});

    private LinkedList<PacketTimer> dstGameTimer = new LinkedList<>();
    private PacketMap dstPacketCounter = new PacketMap(new int[]{78});

    private LinkedList<PacketTimer> inGameTimer = new LinkedList<>();
    private PacketMap inGamePacketCounter = new PacketMap(new int[]{});

    private LinkedList<PacketTimer> spamDetector = new LinkedList<>();

    private int saveCounter = 0;
    private int inGameMaxSize = 100;


    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.Dota2);
        setCountDownLength(45);
        updateStatus(Status.InLobby);

        if(!isCapturing){
            PacketParser.getInstance().start(dotaFilter, this);
            isCapturing = true;
        }
    }


    @Override
    public void resetDetection(){
        super.resetDetection();
        resetQueueTimer();
        resetGameTimer();
        resetInGameTimer();
        saveCounter = 0;
    }

    @Override
    public void saveDetection(){
        super.saveDetection();
        packetQueue = new LinkedList<>();
        // dataHandler.logPackets(packetQueue)
    }

    @Override
    public void saveMissedDetection(){
        super.saveMissedDetection();
        packetQueue = new LinkedList<>();
        //dataHandler.logPackets(packetQueue)
    }

    @Override
    public void stopDetection(){
        if(isCapturing){
            PacketParser.getInstance().terminate();
            isCapturing = false;
        }
        resetDetection();
        super.stopDetection();
    }

    private void resetQueueTimer(){

        queuePort = -1;
        srcQTimer = new LinkedList<>();
        srcQCounter =new PacketMap(new int[]{78,158,270,285});
        dstQTimer = new LinkedList<>();
        dstQCounter =  new PacketMap(new int[]{78, 126, 174, 222});
        stopQTimer = new LinkedList<>();
        stopQCounter =  new PacketMap(new int[]{78,250});
        stopDstQTimer = new LinkedList<>();
        stopDstQCounter = new PacketMap(new int[]{142, 174, 206, 250});
    }

    private void resetGameTimer(){

        spamDetector = new LinkedList<>();

        gameTimer1 = new LinkedList<>();
        packetCounter1 =  new PacketMap(new int[]{600, 700, 800, 900, 1000, 1100, 1200, 1300});

        gameTimer2 = new LinkedList<>();
        packetCounter2 = new PacketMap(new int[]{164, 174, 190, 206});

        dstGameTimer = new LinkedList<>();
        dstPacketCounter = new PacketMap(new int[]{78});

        isProbablyGame = false;
    }

    private void resetInGameTimer(){
        inGameTimer = new LinkedList<>();
        inGamePacketCounter = new PacketMap(new int[]{});
    }


    @Override
    public void update(Packet newPacket) {

        packetQueue.addFirst(newPacket);
        if (packetQueue.size() >= queueMaxSize) {
            packetQueue.removeLast();
        }


        //IN LOBBY
        if(getStatus() == Status.InLobby){

            boolean inGame = isInGame(newPacket, 5, 30);
            boolean gameReady = isGameReady(newPacket);
            boolean startedQueueing = queueStarted(newPacket, 2 * 1000,5, 2);

            if(inGame){updateStatus(Status.InGame);}
            else if(gameReady){updateStatus(Status.GameReady);}
            else if(startedQueueing){updateStatus(Status.InQueue);}
        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){
            boolean inGame = isInGame(newPacket,  5, 30);
            boolean gameReady = isGameReady(newPacket);
            boolean stillQueueing = isStillQueueing(newPacket, 30 * 1000, 5, 2);

            if(inGame){updateStatus(Status.InGame);}
            else if(gameReady){updateStatus(Status.GameReady);}
            else if(!stillQueueing){updateStatus(Status.InLobby);
                resetQueueTimer();
            }
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
            boolean inGame = isInGame(newPacket,  6, 30);
            resetGameTimer();
            if(inGame){updateStatus(Status.InGame);
                resetQueueTimer();}
        }

        //IN GAME
        else  if(getStatus() == Status.InGame){
            boolean inGame = isInGame(newPacket,  6, 30);

            if(!inGame){updateStatus(Status.InLobby);
                resetQueueTimer();
                resetInGameTimer();
            }
        }
    }

    private boolean queueStarted(Packet p, long timeSpan, int maxPacket, int packetNumber) {

        while(!srcQTimer.isEmpty() && p.getCaptureTime() - srcQTimer.getLast().getTime() > timeSpan){
            int key = srcQTimer.removeLast().getKey();
            srcQCounter.put(key, srcQCounter.get(key) - 1);
        }

        while(!dstQTimer.isEmpty() && p.getCaptureTime() - dstQTimer.getLast().getTime() > timeSpan){
            int key = dstQTimer.removeLast().getKey();
            dstQCounter.put(key, dstQCounter.get(key) -1);
        }

        for (int key : srcQCounter.keySet()){
            if((p.getPacketLength() <= key + maxPacket && p.getPacketLength() >= key)
                    && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                srcQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                srcQCounter.put(key, srcQCounter.get(key) + 1);
            }
        }

        for (int key : dstQCounter.keySet()){
            if((p.getPacketLength() <= key + maxPacket && p.getPacketLength() >= key)
                    && p.getDstPort() <= portMax && p.getDstPort() >= portMin){
                dstQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                dstQCounter.put(key, dstQCounter.get(key) + 1);
            }
        }

//        srcQCounter.printMap();
//        dstQCounter.printMap();

        if(dstQCounter.get(174) >= 1 && (srcQCounter.get(270) >= 1 || srcQCounter.get(285) >= 1 ) &&
                (srcQCounter.get(78) >= 1 ||
                        dstQCounter.get(78) >= 1) && (dstQCounter.get(174) + dstQCounter.get(222) >= 2))
        {return true;}
        else if(srcQCounter.get(78) >= 1 && dstQCounter.get(78) >= 1 && dstQCounter.get(174) >= 1 &&
                dstQCounter.get(222) >= 1 &&
                dstQCounter.get(126) >= 1){return true;}
        else if(srcQCounter.get(78) >= 2 && dstQCounter.get(78) >= 1 && dstQCounter.get(222) >= 1 &&
                dstQCounter.get(126) >= 1)
        {return true;}
        else {return false;}
    }


    private boolean isStillQueueing(Packet p, long timeSpan, int maxPacket, int packetNumber) {

        while(!stopQTimer.isEmpty() && p.getCaptureTime() - stopQTimer.getLast().getTime() > timeSpan){
            int key = stopQTimer.removeLast().getKey();
            stopQCounter.put(key, stopQCounter.get(key) -1);
        }

        while(!stopDstQTimer.isEmpty() && p.getCaptureTime() - stopDstQTimer.getLast().getTime() > timeSpan){
            int key = stopDstQTimer.removeLast().getKey();
            stopDstQCounter.put(key, stopDstQCounter.get(key) -1);
        }


        if(p.getPacketLength() <= 250 + 50 && p.getPacketLength() >= 250
                && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
            stopQTimer.addFirst(new PacketTimer(250, p.getCaptureTime()));
            stopQCounter.put(250, stopQCounter.get(250) + 1);
        }

        else if(p.getPacketLength() == 78){
            stopQTimer.addFirst(new PacketTimer(78, p.getCaptureTime()));
            stopQCounter.put(78, stopQCounter.get(78) + 1);
        }


        for (int key : stopQCounter.keySet()){
            if((p.getPacketLength() <= key + 10 && p.getPacketLength() >= key)
                    && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                stopQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                stopQCounter.put(key, stopQCounter.get(key) + 1);
            }
        }

        for (int key : stopDstQCounter.keySet()){
            if((p.getPacketLength() <= key + 10 && p.getPacketLength() >= key)
                    && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                stopDstQTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                stopDstQCounter.put(key, stopDstQCounter.get(key) + 1);
            }
        }


        if(isProbablyGame){return true;}
        if(stopQCounter.get(250) >= 1 && stopDstQCounter.get(142) >= 1 && (stopDstQCounter.get(174) >= 1 ||
                stopDstQCounter.get(206) >= 1)){return false;}
        else if(stopQCounter.get(250) >= 1 && stopDstQCounter.get(142) >= 1 && stopDstQCounter.get(206) >= 1){return
                false;}
        else if(stopQCounter.get(78) >= 2 && stopDstQCounter.get(142) >= 1 && stopDstQCounter.get(206) >= 1){return
                false;}
        else {return true;}
    }


    private boolean isGameReady(Packet p) {

        while(!spamDetector.isEmpty() && p.getCaptureTime() - spamDetector.getLast().getTime() > 1 * 1000){
//            System.out.println(p.getCaptureTime());
//            System.out.println(spamDetector.getLast().getTime());
//            System.out.println(p.getCaptureTime() - spamDetector.getLast().getTime());
            spamDetector.removeLast();
        }

        spamDetector.addFirst(new PacketTimer(p.getPacketLength(), p.getCaptureTime()));

        while(!gameTimer1.isEmpty() && p.getCaptureTime() - gameTimer1.getLast().getTime() > 10 * 1000){
            int key = gameTimer1.removeLast().getKey();
            packetCounter1.put(key, packetCounter1.get(key) - 1);
        }

        while(!dstGameTimer.isEmpty() && p.getCaptureTime() - dstGameTimer.getLast().getTime() > 10 * 1000){
            int key = dstGameTimer.removeLast().getKey();
            dstPacketCounter.put(key, dstPacketCounter.get(key) - 1);
        }

        while(!gameTimer2.isEmpty() && p.getCaptureTime() - gameTimer2.getLast().getTime() > 10 * 1000){
            int key = gameTimer2.removeLast().getKey();
            packetCounter2.put(key, packetCounter2.get(key) - 1);
        }

        if(spamDetector.size() < 20) {
            for (int key : packetCounter1.keySet()) {
                if (p.getPacketLength() <= key + 100 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                        -1) && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin) {
                    gameTimer1.addFirst(new PacketTimer(key, p.getCaptureTime()));
                    packetCounter1.put(key, packetCounter1.get(key) + 1);
                }
            }

            for (int key : dstPacketCounter.keySet()) {
                if (p.getPacketLength() <= key + 5 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                        -1) && p.getDstPort() <= portMax && p.getDstPort() >= portMin) {
                    dstGameTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                    dstPacketCounter.put(key, dstPacketCounter.get(key) + 1);
                }
            }

            for (int key : packetCounter2.keySet()) {
                if (p.getPacketLength() <= key + 5 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                        -1) && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin) {
                    gameTimer2.addFirst(new PacketTimer(key, p.getCaptureTime()));
                    packetCounter2.put(key, packetCounter2.get(key) + 1);
                }
            }
        }

        if(gameTimer1.size() > 0 || gameTimer2.size() > 0 && p.getPacketLength() > 1300){isProbablyGame = true;}
        else{isProbablyGame = false;}
//
        packetCounter1.printMap();
        packetCounter2.printMap();
        dstPacketCounter.printMap();

        if(gameTimer1.size() >= 3
                && packetCounter1.get(1300) < 3
                && gameTimer2.size() > 0
                && dstPacketCounter.get(78) > 1)
        {return true;}

        else if((packetCounter2.get(164) > 0 || packetCounter2.get(174) > 0)
                && packetCounter2.get(190) > 0
                && packetCounter2.get(206) > 0
                && gameTimer1.size() > 0)
        {return true;}
        else if(gameTimer2.size() >= 6){return true;}
        else {return false;}
    }

    private boolean isInGame(Packet p, int timeSpan, int packetNumber) {
        int port = -1;

        if(p.getSrcPort() >= 27000 && p.getSrcPort() <= 28999){port = p.getSrcPort();}
        else if(p.getDstPort() >= 27000 && p.getDstPort() <= 28999){port = p.getDstPort();}

        if(port != -1){
            inGameTimer.addFirst(new PacketTimer(port, p.getCaptureTime()));
            if(inGamePacketCounter.get(port) == null){inGamePacketCounter.put(port, 1);}
            else {inGamePacketCounter.put(port, inGamePacketCounter.get(port) + 1);}
        }

        while(!inGameTimer.isEmpty() && p.getCaptureTime() - inGameTimer.getLast().getTime() > timeSpan || inGameTimer.size() >= inGameMaxSize){
            int key = inGameTimer.removeLast().getKey();
            inGamePacketCounter.put(key, inGamePacketCounter.get(key) - 1);
        }

        int maxNumber = 0;

        for(int key : inGamePacketCounter.keySet()){
            maxNumber = Math.max(maxNumber, inGamePacketCounter.get(key));
        }

        //    println(inGamePacketCounter)
        //    println(maxNumber)
        //    println(inGameTimer.count)
        return maxNumber > 70;
    }
}
