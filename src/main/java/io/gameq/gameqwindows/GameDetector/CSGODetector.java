package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.*;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fabianwikstrom on 8/14/2015.
 */
public class CSGODetector extends PacketDetector {

    private String csgoFilter = "udp src portrange 27000-28000 or udp dst portrange 27000-28000 or udp dst " +
            "port 27005 or udp src port 27015 or udp src port 27005 or udp dst port 27015";

    private int portMin = 27000;
    private int  portMax = 28000;
    private int  uselessPort = -1;
    private int  queuePort = -1;


    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<>();
    private PacketMap packetCounter1 = new PacketMap(new int[]{170});

    private LinkedList<PacketTimer> gameTimer2 = new LinkedList<>();
    private PacketMap packetCounter2 = new PacketMap(new int[]{60, 590});

    private LinkedList<PacketTimer> dstGameTimer = new LinkedList<>();
    private PacketMap dstPacketCounter = new PacketMap(new int[]{75, 190});

    private LinkedList<PacketTimer> inGameTimer = new LinkedList<>();

    private boolean foundServer = false;
    private boolean soonGame = false;

    private int inGameMaxSize = 101;
    private double time = -1;
    private Timer timer = null;

    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.CSGO);
        setCountDownLength(20);
        updateStatus(Status.InQueue);

        if(!isCapturing){
            //new thread?????
            PacketParser.getInstance().start(csgoFilter, this);
            isCapturing = true;
        }
    }

    @Override
    public void resetDetection(){
        super.resetDetection();
        resetGameTimer();
        resetInGameTimer();
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


    public void resetGameTimer(){

        foundServer = false;
        soonGame = false;

        gameTimer1 = new LinkedList<>();
        packetCounter1 = new PacketMap(new int[]{170});

        gameTimer2 = new LinkedList<>();
        packetCounter2 = new PacketMap(new int[]{60, 590});

        dstGameTimer = new LinkedList<>();
        dstPacketCounter = new PacketMap(new int[]{75, 190});

        if(timer != null) {
            timer.cancel();
            timer.purge();
        }
        time = -1.0;
    }

    public void resetInGameTimer(){
        inGameTimer = new LinkedList<>();
    }

    @Override
    protected void update(Packet newPacket) {

        //IN LOBBY
        if(getStatus() == Status.InLobby){
            updateStatus(Status.InQueue);
        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){

            boolean inGame = isGame(newPacket, 10, 0, 90);
            boolean gameReady = isGameReady(newPacket);

            if(inGame){updateStatus(Status.InGame);}
            else if(gameReady){updateStatus(Status.GameReady);
                resetGameTimer();}
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
            boolean inGame = isGame(newPacket, 10, 0, 90);
            if(inGame){updateStatus(Status.InGame);
                resetGameTimer();}
        }

        //IN GAME
        else if(getStatus() == Status.InGame){
            resetGameTimer();
            boolean inGame = isGame(newPacket, 10, 0, 90);
            if(!inGame){updateStatus(Status.InQueue);}
        }

        else {
        }
    }

    private boolean isGameReady(Packet p) {

        while(!gameTimer1.isEmpty() && p.getCaptureTime() - gameTimer1.getLast().getTime() > 60.0){
            int key = gameTimer1.removeLast().getKey();
            packetCounter1.put(key, packetCounter1.get(key) - 1);
        }

        double t = 1.0;
        if(isTesting()){t = 0.2;}
        else{t = 2.0;}

        while(!gameTimer2.isEmpty() && p.getCaptureTime() - gameTimer2.getLast().getTime() > t){
            int key = gameTimer2.removeLast().getKey();
            packetCounter2.put(key, packetCounter2.get(key) - 1);
        }

        while(!dstGameTimer.isEmpty() && p.getCaptureTime() - dstGameTimer.getLast().getTime() > 10.0){
            int key = dstGameTimer.removeLast().getKey();
            dstPacketCounter.put(key, dstPacketCounter.get(key) - 1);
        }


        for (int key: packetCounter1.keySet()){
            if(p.getPacketLength() <= key + 30 && p.getPacketLength() >= key && (p.getSrcPort() == queuePort ||
                    queuePort == -1) && p.getSrcPort() <= portMax && p.getSrcPort() >= portMin){
                gameTimer1.addFirst(new PacketTimer(key, p.getCaptureTime()));
                packetCounter1.put(key, packetCounter1.get(key) + 1);
            }
        }

        for (int key: packetCounter2.keySet()){
            if(p.getPacketLength() <= key && p.getPacketLength() >= key && p.getDstPort() == 27005){
                gameTimer2.addFirst(new PacketTimer(key, p.getCaptureTime()));
                packetCounter2.put(key, packetCounter2.get(key) + 1);
            }
        }

        for (int key: dstPacketCounter.keySet()){
            if(p.getPacketLength() <= key && p.getPacketLength() >= key && p.getDstPort() != 27015){
                dstGameTimer.addFirst(new PacketTimer(key, p.getCaptureTime()));
                dstPacketCounter.put(key, dstPacketCounter.get(key) + 1);
            }
        }

        if(gameTimer1.size() >= 30){foundServer = true;}
        //else{foundServer = false}

        if(packetCounter2.get(60) > 0 && soonGame == false){
            soonGame = true;
            time = p.getCaptureTime();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    update();
                }
            }, 0, 200);

//            dispatch_async(dispatch_get_main_queue()) {
//                self.timer = NSTimer.scheduledTimerWithTimeInterval(0.2, target: self, selector: Selector("update3"), userInfo: nil, repeats: true)
//            }
        }

        if(soonGame == true && packetCounter2.get(590) > 0) {
            soonGame = false;
            time = -1.0;
            timer.purge();
            timer.cancel();
        }



        if(soonGame == true && packetCounter2.get(60) <= 0 && foundServer){return true;}
        else{ return false;}

    }

    private boolean isGame(Packet p, double timeSpan, int maxPacket, int packetNumber) {
        while(!inGameTimer.isEmpty() && p.getCaptureTime() - inGameTimer.getLast().getTime() > timeSpan || inGameTimer.size() >= inGameMaxSize){
            int key = inGameTimer.removeLast().getKey();
        }

        if(p.getPacketLength() >= 200){
            inGameTimer.addFirst(new PacketTimer(p.getDstPort(), p.getCaptureTime()));
        }

        if(inGameTimer.size() >= packetNumber){return true;}
        else {return false;}
    }

    public void update(){
        if(soonGame){
            time = time + 0.2;
            if(isGameReady(new Packet(-1,  -1,  -1, time))){
                updateStatus(Status.GameReady);
                timer.purge();
                timer.cancel();
            }
        }
    }
}
