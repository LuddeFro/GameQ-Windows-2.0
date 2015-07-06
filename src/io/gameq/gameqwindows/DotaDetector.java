package io.gameq.gameqwindows;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class DotaDetector extends GameDetector implements PacketDetector {

    private String dotaFilter = "udp src portrange 27000-28999 or udp dst portrange 27000-28999";
    private LinkedList<Packet> packetQueue = new LinkedList<Packet>();
    private int queueMaxSize = 200;
    private boolean isCapturing = false;

    private int portMin = 27000;
    private int portMax = 27050;

    private int queuePort = -1;
    private boolean isProbablyGame = false;

    private LinkedList<PacketTimer> srcQTimer = new LinkedList<PacketTimer>();
    private PacketHashMap srcQCounter = new PacketHashMap(new int[]{78,158,270,285});

    private LinkedList<PacketTimer> dstQTimer = new LinkedList<PacketTimer>();
    private PacketHashMap dstQCounter = new PacketHashMap(new int[]{126,142,174,222});

    private LinkedList<PacketTimer> stopQTimer = new LinkedList<PacketTimer>();
    private PacketHashMap stopQCounter = new PacketHashMap(new int[]{78,250});

    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<PacketTimer>();
    private PacketHashMap packetCounter1 = new PacketHashMap(new int[]{600, 700, 800, 900, 1000, 1100, 1200, 1300});

    private LinkedList<PacketTimer> gameTimer2 = new LinkedList<PacketTimer>();
    private PacketHashMap packetCounter2 = new PacketHashMap(new int[]{164, 174, 190, 206});

    private LinkedList<PacketTimer> dstGameTimer = new LinkedList<PacketTimer>();
    private PacketHashMap dstPacketCounter = new PacketHashMap(new int[]{78});

    private LinkedList<PacketTimer> inGameTimer = new LinkedList<PacketTimer>();
    private HashMap<Integer,Integer> inGamePacketCounter = new HashMap<Integer, Integer>();

    private int saveCounter = 0;


    @Override
    public void startDetection() {
        setGame(Game.Dota2);
        //self.detector = self
        setCountDownLength(45);
        updateStatus(Status.InLobby);
        super.startDetection();

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
        packetQueue = new LinkedList<Packet>();
        // dataHandler.logPackets(packetQueue)
    }

    @Override
    public void saveMissedDetection(){
        super.saveMissedDetection();
        packetQueue = new LinkedList<Packet>();
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
        srcQTimer = new LinkedList<PacketTimer>();
        srcQCounter = new PacketHashMap(new int[]{78,158,270,285});
        dstQTimer = new LinkedList<PacketTimer>();
        dstQCounter = new PacketHashMap(new int[]{126,142,174,222});
        stopQTimer = new LinkedList<PacketTimer>();
        stopQCounter =  new PacketHashMap(new int[]{78,250});
    }

    private void resetGameTimer(){
        gameTimer1 = new LinkedList<PacketTimer>();
        packetCounter1 =  new PacketHashMap(new int[]{600, 700, 800, 900, 1000, 1100, 1200, 1300});

        gameTimer2 = new LinkedList<PacketTimer>();
        packetCounter2 = new PacketHashMap(new int[]{164, 174, 190, 206});

        dstGameTimer = new LinkedList<PacketTimer>();
        dstPacketCounter = new PacketHashMap(new int[]{78});

        isProbablyGame = false;
    }

    private void resetInGameTimer(){
        inGameTimer = new LinkedList<PacketTimer>();
        inGamePacketCounter = new HashMap<Integer, Integer>();
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

        printMap(srcQCounter);
        printMap(dstQCounter);

//        //bad coode here yo
//        if(srcQCounter.get(78) > 0 && srcQCounter.get(158) > 0
//                || (dstQCounter.get(174) > 0 && srcQCounter.get(78) > 0 && (srcQCounter.get(270) > 0 || srcQCounter.get(285) > 0 ))){
//
//            srcQTimer.add(new PacketTimer(158, p.getCaptureTime()));
//            srcQCounter.put(158, srcQCounter.get(158) + 1);
//
//            srcQTimer.add(new PacketTimer(78, p.getCaptureTime()));
//            srcQCounter.put(78, srcQCounter.get(78) + 1);
//
//            dstQTimer.add(new PacketTimer(126, p.getCaptureTime()));
//            dstQCounter.put(126, dstQCounter.get(126) + 1);
//
//            dstQTimer.add(new PacketTimer(142, p.getCaptureTime()));
//            dstQCounter.put(142, dstQCounter.get(142) + 1);
//
//            return true;
//        }
//        else {return false;}
        return false;
    }


    private boolean isStillQueueing(Packet newPacket, int timeSpan, int maxPacket, int packetNumber) {
        return false;
    }

    private boolean isInGame(Packet newPacket, int timeSpan, int packetNumber) {
        return false;
    }


    private boolean isGameReady(Packet newPacket) {
        return false;
    }

    public void printMap(HashMap<Integer,Integer> map){
        for (int name: map.keySet()){
            String value = map.get(name).toString();
            System.out.print(name + " : " + value + ", ");
        }
        System.out.println();
    }
}
