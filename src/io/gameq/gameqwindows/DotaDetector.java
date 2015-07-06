package io.gameq.gameqwindows;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class DotaDetector extends GameDetector implements PacketDetector {

    private String dotaFilter = "udp src portrange 27000-28999 or udp dst portrange 27000-28999";
    private LinkedList<Packet> packetQueue = null;
    private int queueMaxSize = -1;
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
            PacketParser.getInstance().start(dotaFilter);
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

    private boolean isStillQueueing(Packet newPacket, int timeSpan, int maxPacket, int packetNumber) {
        return false;
    }

    private boolean queueStarted(Packet newPacket, int timeSpan, int maxPacket, int packetNumber) {
        return false;
    }


    private boolean isInGame(Packet newPacket, int timeSpan, int packetNumber) {
        return false;
    }


    private boolean isGameReady(Packet newPacket) {
        return false;
    }

}
