package io.gameq.gameqwindows.GameDetector;

import io.gameq.gameqwindows.DataHandler.DataHandler;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public abstract class GameDetector {

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
    //private countdownTimer .....


    public void startDetection(){
        //datahandler.foldername = ...
        counter = 0;
    }

    public void updateStatus(Status newStatus){
        if(newStatus == Status.InLobby || newStatus == Status.InQueue){
            counter = 0;
        }

        if(status != newStatus && newStatus == Status.GameReady && !isTesting){
            //detector.saveDetection()
            //startTimer()
        }

        else{
            // countDownTimer.invalidate()
            counter = 0;
        }

        status = newStatus;
        System.out.println("new Status: " + status);
        // NSNotificationCenter.defaultCenter().postNotificationName("updateStatus", object: nil)
    }


    public void saveDetection(){
        System.out.println("Saving File");
        //dataHandler.folderName = game.rawValue
    }

    public void resetDetection() {
        updateStatus(Status.InLobby);
    }

    public void saveMissedDetection(){
        System.out.println("Saving Missed File");
        //dataHandler.folderName = game.rawValue + "missed"
    }

    public void failMode(){

        if(isFailMode){
            System.out.println("FailMode Off");
           // dataHandler.folderName = game.rawValue
            isFailMode = false;
        }

        else{
            System.out.println("FailMode On");
            //dataHandler.folderName = game.rawValue + "ForcedFails"
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
    }

    public void startTimer(){

//        dispatch_async(dispatch_get_main_queue()) {
//            self.countDownTimer = NSTimer.scheduledTimerWithTimeInterval(1, target: self, selector: Selector("update"), userInfo: nil, repeats: true)}
    }



    protected Game getGame() {
        return game;
    }

    protected void setGame(Game game) {
        this.game = game;
    }

    protected Status getStatus() {
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

    protected int getCountDownLength() {
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
