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
    private PacketMap packetCounter1 = new PacketMap(new int[]{170});

    private LinkedList<PacketTimer> dstGameTimer = new LinkedList<>();
    private PacketMap dstPacketCounter = new PacketMap(new int[]{75});

    private LinkedList<PacketTimer> gameTimer2 = new LinkedList<>();
    private PacketMap packetCounter2 = new PacketMap(new int[]{60, 590});

    private LinkedList<PacketTimer> inGameTimer = new LinkedList<>();

    private boolean foundServer = false;
    private boolean soonGame = false;


    @Override
    public void startDetection(Main application) {
        super.startDetection(application);
        setGame(Game.Dota2);
        //self.detector = self
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

        foundServer = false;
        soonGame = false;

        gameTimer1 = new LinkedList<>();
        packetCounter1 = new PacketMap(new int[]{170});

        gameTimer2 = new LinkedList<>();
        packetCounter2 =  new PacketMap(new int[]{60, 590});

        dstGameTimer = new LinkedList<>();
        dstPacketCounter = new PacketMap(new int[]{75});

        inGameTimer = new LinkedList<>();

    }

    @Override
    protected void update(Packet newPacket) {

        //IN LOBBY
        if(getStatus() == Status.InLobby){

        }

        //IN QUEUE
        else  if(getStatus() == Status.InQueue){
            boolean inGame = isGame(newPacket, timeSpan:10, maxPacket:0, packetNumber:50);
            updateStatus(Status.GameReady);
        }

        //GAME READY
        else if(getStatus() == Status.GameReady){
            boolean inGame = isGame(newPacket, timeSpan:10, maxPacket:0, packetNumber:50);
            if(inGame){updateStatus(Status.InGame);
            }
        }

        //IN GAME
        else  if(getStatus() == Status.InGame){
            boolean inGame = isGame(newPacket, timeSpan:10, maxPacket:0, packetNumber:50);
            if(!inGame){updateStatus(Status.InLobby);
            }
        }

        else {
        }
    }
}
