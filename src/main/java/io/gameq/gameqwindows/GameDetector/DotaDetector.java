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
    private QueueChecker checker = null;

    private int portMin = 27000;
    private int portMax = 27050;



    private LinkedList<PacketTimer> inGameTimer = new LinkedList<>();
    private PacketMap inGamePacketCounter = new PacketMap(new int[]{});

    private int saveCounter = 0;
    private int inGameMaxSize = 100;


    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.Dota2);
        setCountDownLength(45);
        updateStatus(Status.InLobby);
        checker = new QueueChecker();
        checker.startMonitor(Game.Dota2, this);

        if(!isCapturing){
            PacketParser.getInstance().start(dotaFilter, this);
            isCapturing = true;
        }
    }


    @Override
    public void resetDetection(){
        super.resetDetection();
        resetInGameTimer();
        saveCounter = 0;
    }

    @Override
    public void stopDetection(){
        if(isCapturing){
            PacketParser.getInstance().terminate();
            isCapturing = false;
        }
        resetDetection();
        checker.stopMonitor(Game.Dota2);
        checker = null;
        super.stopDetection();
    }

    private void resetInGameTimer(){
        inGameTimer = new LinkedList<>();
        inGamePacketCounter = new PacketMap(new int[]{});
    }


    @Override
    public void update(Packet newPacket) {

        //IN LOBBY
        if(getStatus() == Status.InLobby){

            boolean inGame = isInGame(newPacket, 5, 30);

            if(inGame){
                updateStatus(Status.InGame);
            }
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
            boolean inGame = isInGame(newPacket,  6, 30);
            if(inGame){
                updateStatus(Status.InGame);
            }
        }

        //IN GAME
        else  if(getStatus() == Status.InGame){
            boolean inGame = isInGame(newPacket,  6, 30);

            if(!inGame){
                updateStatus(Status.InLobby);
                resetInGameTimer();
            }
        }
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

        return maxNumber > 70;
    }
}
