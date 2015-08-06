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
    private PacketMap dstQCounter = new PacketMap(new int[]{126,142,174,222});

    private LinkedList<PacketTimer> stopQTimer = new LinkedList<>();
    private PacketMap stopQCounter = new PacketMap(new int[]{78,250});

    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<>();
    private PacketMap packetCounter1 = new PacketMap(new int[]{600, 700, 800, 900, 1000, 1100, 1200, 1300});

    private LinkedList<PacketTimer> gameTimer2 = new LinkedList<>();
    private PacketMap packetCounter2 = new PacketMap(new int[]{164, 174, 190, 206});

    private LinkedList<PacketTimer> dstGameTimer = new LinkedList<>();
    private PacketMap dstPacketCounter = new PacketMap(new int[]{78});

    private LinkedList<PacketTimer> inGameTimer = new LinkedList<>();
    private PacketMap inGamePacketCounter = new PacketMap(new int[]{});

    private int saveCounter = 0;
    private int inGameMaxSize = 100;


    @Override
    public void startDetection(Main application) {
        setGame(Game.Dota2);
        //self.detector = self
        setCountDownLength(45);
        updateStatus(Status.InLobby);
        super.startDetection(application);

        if(!isCapturing){
            //new thread?????
            PacketParser.getInstance().start(dotaFilter, this);
        }
        isCapturing = true;
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
        srcQCounter = new PacketMap(new int[]{78,158,270,285});
        dstQTimer = new LinkedList<>();
        dstQCounter = new PacketMap(new int[]{126,142,174,222});
        stopQTimer = new LinkedList<>();
        stopQCounter =  new PacketMap(new int[]{78,250});
    }

    private void resetGameTimer(){
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
    public void handle(Packet newPacket) {
        update(newPacket);
    }

    @Override
    public void handleTest(Packet newPacket) {
        update(newPacket);
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
            boolean startedQueueing = queueStarted(newPacket, 30,5, 2);

            if(inGame){updateStatus(Status.InGame);}
            else if(gameReady){updateStatus(Status.GameReady);}
            else if(startedQueueing){updateStatus(Status.InQueue);}
        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){
            boolean inGame = isInGame(newPacket,  5, 30);
            boolean gameReady = isGameReady(newPacket);
            boolean stillQueueing = isStillQueueing(newPacket, 30, 5, 2);

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

    private boolean queueStarted(Packet p, int timeSpan, int maxPacket, int packetNumber) {

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

        srcQCounter.printMap();
        dstQCounter.printMap();

        //bad coode here yo
        if(srcQCounter.get(78) > 0 && srcQCounter.get(158) > 0
                || (dstQCounter.get(174) > 0 && srcQCounter.get(78) > 0 && (srcQCounter.get(270) > 0 || srcQCounter.get(285) > 0 ))){

            srcQTimer.add(new PacketTimer(158, p.getCaptureTime()));
            srcQCounter.put(158, srcQCounter.get(158) + 1);

            srcQTimer.add(new PacketTimer(78, p.getCaptureTime()));
            srcQCounter.put(78, srcQCounter.get(78) + 1);

            dstQTimer.add(new PacketTimer(126, p.getCaptureTime()));
            dstQCounter.put(126, dstQCounter.get(126) + 1);

            dstQTimer.add(new PacketTimer(142, p.getCaptureTime()));
            dstQCounter.put(142, dstQCounter.get(142) + 1);

            return true;
        }
        else {return false;}
    }


    private boolean isStillQueueing(Packet p, int timeSpan, int maxPacket, int packetNumber) {

        while(!srcQTimer.isEmpty() && p.getCaptureTime() - srcQTimer.getLast().getTime() > timeSpan){
            int key = srcQTimer.removeLast().getKey();
            srcQCounter.put(key, srcQCounter.get(key) - 1);
        }

        while(!dstQTimer.isEmpty() && p.getCaptureTime() - dstQTimer.getLast().getTime() > timeSpan){
            int key = dstQTimer.removeLast().getKey();
            dstQCounter.put(key, dstQCounter.get(key) -1);
        }


        while(!stopQTimer.isEmpty() && p.getCaptureTime() - stopQTimer.getLast().getTime() > timeSpan){
            int key = stopQTimer.removeLast().getKey();
            stopQCounter.put(key, stopQCounter.get(key) -1);
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

        if(p.getPacketLength() <= 250 + 50 && p.getPacketLength() >= 250
                && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
            stopQTimer.addFirst(new PacketTimer(250, p.getCaptureTime()));
            stopQCounter.put(250, stopQCounter.get(250) + 1);
        }

        else if(p.getPacketLength() == 78){
            stopQTimer.addFirst(new PacketTimer(78, p.getCaptureTime()));
            stopQCounter.put(78, stopQCounter.get(78) + 1);
        }



        if(isProbablyGame){return true;}
        if(stopQCounter.get(78) > 1 && stopQCounter.get(250) > 0){return false;}
        //else if(srcQueueCounter[78] > 0 && srcQueueCounter[158] > 0 || isProbablyGame){return true}
        if((srcQCounter.get(78) + srcQCounter.get(158) + dstQCounter.get(126) + dstQCounter.get(142) > 2)
                && ((srcQCounter.get(78) > 0 && srcQCounter.get(78)  > 0 &&
                ( dstQCounter.get(126) > 0 ||  dstQCounter.get(142)  > 0))))
        {return true;}
        else {return false;}
    }


    private boolean isGameReady(Packet p) {

        while(!gameTimer1.isEmpty() && p.getCaptureTime() - gameTimer1.getLast().getTime() > 10){
            int key = gameTimer1.removeLast().getKey();
            packetCounter1.put(key, packetCounter1.get(key) - 1);
        }

        while(!dstGameTimer.isEmpty() && p.getCaptureTime() - dstGameTimer.getLast().getTime() > 10){
            int key = dstGameTimer.removeLast().getKey();
            dstPacketCounter.put(key, dstPacketCounter.get(key) - 1);
        }

        while(!gameTimer2.isEmpty() && p.getCaptureTime() - gameTimer2.getLast().getTime() > 10){
            int key = gameTimer2.removeLast().getKey();
            packetCounter2.put(key, packetCounter2.get(key) - 1);
        }

        for (int key: packetCounter1.keySet()){
            if(p.getPacketLength() <= key + 100 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                    -1) && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                gameTimer1.addFirst(new PacketTimer(key, p.getCaptureTime()));
                packetCounter1.put(key, packetCounter1.get(key) + 1);
            }
        }

        for (int key: dstPacketCounter.keySet()){
            if(p.getPacketLength() <= key + 5 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                    -1) && p.getDstPort() <= portMax && p.getDstPort() >= portMin){
                dstGameTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                dstPacketCounter.put(key, dstPacketCounter.get(key) + 1);
            }
        }

        for (int key: packetCounter2.keySet()){
            if(p.getPacketLength() <= key + 5 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort || queuePort ==
                    -1) && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                gameTimer2.addFirst(new PacketTimer(key, p.getCaptureTime()));
                packetCounter2.put(key, packetCounter2.get(key) + 1);
            }
        }

        if(gameTimer1.size() > 0 || gameTimer2.size() > 0 && p.getPacketLength() > 1300){isProbablyGame = true;}
        else{isProbablyGame = false;}

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
