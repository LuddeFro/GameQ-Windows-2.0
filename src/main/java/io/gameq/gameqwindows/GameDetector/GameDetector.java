package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.DataHandler.DataHandler;
import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public abstract class GameDetector {


    private Main application = null;
    private Game game = Game.NoGame;
    private Status status = Status.Online;
    private boolean isFailMode = false;
    private boolean isTesting = false;

    private DataHandler dataHandler = DataHandler.getInstance();
    private boolean saveToDesktop = false;
    private boolean saveToServer = true;
    private String saveMemory = "";
    private Game lastGame = Game.NoGame;

    private int countDownLength = -1;
    private int counter = -1;
    private Timer timer;


    public void startDetection(Main application){
        counter = 0;
        this.application = application;
    }

    public void updateStatus(Status newStatus) {
        if (newStatus == Status.InLobby || newStatus == Status.InQueue) {
            counter = 0;
        }

        if (status != newStatus && newStatus == Status.GameReady && !isTesting) {
            //detector.saveDetection()
            startTimer();
            saveDetection();
        } else {
            if(timer != null){
                timer.cancel();
                timer.purge();
            }
        }

        status = newStatus;
        System.out.println("new Status: " + status);

        if (!isTesting) {
            if(newStatus == Status.InLobby){
                newStatus = Status.InQueue;
            }
            application.updateStatus(newStatus);
        }
    }


    public void saveDetection(){
//        System.out.println(application.fileToString());
        if(saveToServer){
            ConnectionHandler.submitCSV(((success, error) -> {
                if(success){
                    System.out.println("Submitted CSV");
                } else{
                    System.out.println("Failed to submit CSv");
                }
            }),application.fileToString(), Encoding.getIntFromGame(game), 3);
        }
    }

    public void resetDetection() {
    }

    public void saveMissedDetection(){
        System.out.println("Saving Missed File");
    }

    public void failMode(){

        if(isFailMode){
            System.out.println("FailMode Off");
            isFailMode = false;
        }

        else{
            System.out.println("FailMode On");
            isFailMode = true;
        }
    }

    public void stopDetection(){
        System.out.println("Stopping Detection");
        this.game = Game.NoGame;
        updateStatus(Status.Online);
        isFailMode = false;
        isTesting = false;
        counter = -1;
        countDownLength = -1;
        application.setMemory(fileToString());
    }

    public void startTimer(){
        System.out.println(this.countDownLength);
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, countDownLength * 1000, countDownLength * 1000);
    }

    public void update(){
        if(status == Status.GameReady){
            updateStatus(Status.InGame);
            timer.cancel();
            timer.purge();
        }
    }

    public String fileToString(){
        return "";
    }


    public Game getGame() {
        return game;
    }

    protected void setGame(Game game) {
        this.game = game;
    }

    public Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    protected boolean isFailmode() {
        return isFailMode;
    }

    protected void setFailmode(boolean failmode) {
        this.isFailMode = failmode;
    }

    protected boolean isTesting() {
        return isTesting;
    }

    protected void setIsTesting(boolean isTesting) {
        this.isTesting = isTesting;
    }

    public int getCountDownLength() {
        return countDownLength;
    }

    protected int getCounter() {
        return counter;
    }

    protected void setCounter(int counter) {
        this.counter = counter;
    }

    protected void setCountDownLength(int countDownLength) {
        this.countDownLength = countDownLength;
    }
}
