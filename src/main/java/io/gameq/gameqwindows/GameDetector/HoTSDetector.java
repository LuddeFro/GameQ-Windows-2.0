package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.*;

import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 8/13/2015.
 */
public class HoTSDetector extends PacketDetector {

    private String HOTSFilter = "udp src port 1119 or udp src port 6113 or udp src port 1120 or udp src port 80 or" +
            " udp src port 3724 or udp dst port 1119 or udp dst port 6113 or udp dst port 1120 or udp dst port 80 or udp dst port 3724";

    private LinkedList<PacketTimer> gameTimer1 = new LinkedList<>();

    private int inGameMaxSize = 101;


    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.HoTS);
        setCountDownLength(10);
        updateStatus(Status.InQueue);

        if (!isCapturing) {
            //new thread?????
            PacketParser.getInstance().start(HOTSFilter, this);
            isCapturing = true;
        }
    }

    @Override
    public void resetDetection(){
        super.resetDetection();
        resetGameTimer();
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
        gameTimer1 = new LinkedList<>();
    }

    @Override
    protected void update(Packet newPacket) {

        //IN LOBBY
        if(getStatus() == Status.InLobby){

        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){
            boolean inGame = isGame(newPacket, 10.0, 0, 50);
            updateStatus(Status.GameReady);
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
            boolean inGame = isGame(newPacket, 10.0, 0, 50);
            if(inGame){updateStatus(Status.InGame);
            }
        }

        //IN GAME
        else  if(getStatus() == Status.InGame){
            boolean inGame = isGame(newPacket, 10.0, 0, 50);
            if(!inGame){updateStatus(Status.InLobby);
            }
        }

        else {
        }
    }

    public boolean isGame(Packet p, Double timeSpan, int maxPacket, int packetNumber){

        while(!gameTimer1.isEmpty() && p.getCaptureTime() - gameTimer1.getLast().getTime() > timeSpan || gameTimer1
                .size() >= inGameMaxSize){
            gameTimer1.removeLast();
        }

        gameTimer1.addFirst(new PacketTimer(p.getSrcPort(), p.getCaptureTime()));

        if(gameTimer1.size() >= packetNumber){return true;}
        else {return false;}
    }
}
