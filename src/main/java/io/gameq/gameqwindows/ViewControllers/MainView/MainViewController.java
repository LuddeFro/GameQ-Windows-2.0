package io.gameq.gameqwindows.ViewControllers.MainView;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.ViewControllers.MainView.ProgressTimer.RingProgressIndicator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainViewController extends VBox implements Initializable {

    @FXML Button startButton;
    @FXML Button stopButton;
    @FXML Button saveButton;
    @FXML Button saveMissed;
    @FXML Button failModeButton;
    @FXML Button logOutButton;
    @FXML AnchorPane anchorPane;
    @FXML StackPane timerHolder;

    private String userName;

    public void startButtonClicked(){
       startTimer();
    }

    public void stopButtonClicked(){
        System.out.println("stop");
    }

    public void saveButtonClicked(){
        System.out.println("save");
    }

    public void saveMissedButtonClicked(){
        System.out.println("save miss");
    }

    public void failModeButtonClicked(){
        System.out.println("failmode");
    }

    public void logOutButtonClicked(){
        processLogout();
    }


    private Main application;
    private Timeline timer = null;
    private double counter = 0;
    private double countDownTime;
    private RingProgressIndicator countDownIndicator =  new RingProgressIndicator();
    Timeline fiveSecondsWonder = null;

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        countDownIndicator.setProgress(50);
        timerHolder.getChildren().add(countDownIndicator);
        StackPane.setAlignment(countDownIndicator, Pos.CENTER);

        countDownTime = 45;
        if(true) {
            startButton.setDisable(true);
            startButton.setVisible(false);
            stopButton.setDisable(true);
            stopButton.setVisible(false);
            failModeButton.setDisable(true);
            failModeButton.setVisible(false);
            saveButton.setDisable(true);
            saveButton.setVisible(false);
            saveMissed.setDisable(true);
            saveMissed.setVisible(false);
        }
    }

    public void processLogout() {
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            return;
        }

        application.userLogout();
    }

    private void startTimer(){
        fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            this.counter = this.counter + 0.1;
            System.out.println(counter);
            countDownIndicator.setProgress(this.counter/countDownTime * 100);
        }));
        fiveSecondsWonder.setCycleCount((int) countDownTime * 10);
        fiveSecondsWonder.play();
    }

    public void setCountDownTime(double countDownTime) {
        this.countDownTime = countDownTime;
    }
}
