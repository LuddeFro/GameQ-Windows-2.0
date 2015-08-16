package io.gameq.gameqwindows.ViewControllers.MainView;

import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;
import io.gameq.gameqwindows.ViewControllers.FeedbackView.FeedbackController;
import io.gameq.gameqwindows.ViewControllers.MainView.ProgressTimer.RingProgressIndicator;
import io.gameq.gameqwindows.ViewControllers.SettingsView.SettingsController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController extends VBox implements Initializable {

    @FXML Button startButton;
    @FXML Button stopButton;
    @FXML Button saveButton;
    @FXML Button saveMissed;
    @FXML Button failModeButton;
    @FXML AnchorPane anchorPane;
    @FXML StackPane timerHolder;
    @FXML Label statusLabel;
    @FXML Label gameLabel;
    @FXML ImageView feedbackButton;
    @FXML ImageView settingsButton;
    @FXML ImageView exitButton;
    @FXML ImageView minButton;

    private boolean isFeedback = false;
    private boolean isSettings = false;
    private Stage feedback = null;
    private Stage settings = null;

    class Delta { double x, y; }

    public void exitPressed(){
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // do what you have to do
        Platform.runLater(stage::close);
    }

    public void minPressed(){
        Stage stage = (Stage) minButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void startButtonClicked(){
        application.updateStatus(Status.GameReady);
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

    public void feedbackClicked() {

        if(isFeedback){
            Platform.runLater(feedback::show);
            Platform.runLater(feedback::toFront);
        }
        else {
            if(isSettings){
                Platform.runLater(() -> {
                    settings.close();
                    settings = null;
                    isSettings = false;});
            }

            Platform.runLater(() -> {

                FXMLLoader loader = new FXMLLoader();
                loader.setBuilderFactory(new JavaFXBuilderFactory());
                loader.setLocation(getClass().getClassLoader().getResource("ViewControllers/FeedbackView/FeedbackView" +
                        ".fxml"));
                VBox page = null;
                try (InputStream in = getClass().getClassLoader().getResourceAsStream
                        ("ViewControllers/FeedbackView/FeedbackView.fxml")) {
                    page = loader.load(in);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                FeedbackController controller = loader.getController();
                controller.setApp(application);

                feedback = new Stage();
                feedback.setTitle("Feedback");
                feedback.setScene(new Scene(page, 400, 500));
                feedback.setResizable(false);
                feedback.initStyle(StageStyle.UNDECORATED);
                feedback.show();
                isFeedback = true;

                // allow the background to be used to drag the clock around.
                final Delta dragDelta = new Delta();
                if (page != null) {
                    page.setOnMousePressed(mouseEvent -> {
                        // record a delta distance for the drag and drop operation.
                        dragDelta.x = feedback.getX() - mouseEvent.getScreenX();
                        dragDelta.y = feedback.getY() - mouseEvent.getScreenY();
                    });
                    page.setOnMouseDragged(mouseEvent -> {
                        feedback.setX(mouseEvent.getScreenX() + dragDelta.x);
                        feedback.setY(mouseEvent.getScreenY() + dragDelta.y);
                    });
                }
            });
        }
    }

    public void settingsClicked(){
        if(isSettings){
            Platform.runLater(settings::show);
            Platform.runLater(settings::toFront);
        }

        else {
            if(isFeedback){
                Platform.runLater(() -> {
                    feedback.close();
                    feedback = null;
                    isFeedback = false;});
            }

            Platform.runLater(() -> {
                FXMLLoader loader = new FXMLLoader();
                loader.setBuilderFactory(new JavaFXBuilderFactory());
                loader.setLocation(getClass().getClassLoader().getResource("ViewControllers/SettingsView/SettingsView" +
                        ".fxml"));
                VBox page = null;
                try (InputStream in = getClass().getClassLoader().getResourceAsStream
                        ("ViewControllers/SettingsView/SettingsView.fxml")) {
                    page = loader.load(in);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                SettingsController controller = loader.getController();
                controller.setApp(application);
                settings = new Stage();
                settings.setTitle("Settings");
                settings.setScene(new Scene(page, 400, 500));
                settings.setResizable(false);
                settings.initStyle(StageStyle.UNDECORATED);
                settings.show();
                isSettings = true;

                // allow the background to be used to drag the clock around.
                final Delta dragDelta = new Delta();
                if (page != null) {
                    page.setOnMousePressed(mouseEvent -> {
                        // record a delta distance for the drag and drop operation.
                        dragDelta.x = settings.getX() - mouseEvent.getScreenX();
                        dragDelta.y = settings.getY() - mouseEvent.getScreenY();
                    });
                    page.setOnMouseDragged(mouseEvent -> {
                        settings.setX(mouseEvent.getScreenX() + dragDelta.x);
                        settings.setY(mouseEvent.getScreenY() + dragDelta.y);
                    });
                }
            });
        }
    }

    private Main application;
    private Timeline timer = null;
    private double counter = 0;
    private RingProgressIndicator countDownIndicator =  new RingProgressIndicator();
    Timeline fiveSecondsWonder = null;

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            countDownIndicator.setProgress(0);
            timerHolder.getChildren().add(countDownIndicator);
            StackPane.setAlignment(countDownIndicator, Pos.CENTER);

            if (false) {
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
        });
    }

    public void updateStatus(Game game, Status status, double countDownTime){

        Platform.runLater(() -> this.statusLabel.setText(Encoding.getStringFromGameStatus(game, status)));
        Platform.runLater(() -> this.gameLabel.setText(Encoding.getStringFromGame(game)));

        switch (status){
            case Offline:
                resetTimer(false);
                break;
            case Online:
                resetTimer(false);
                break;
            case InLobby:
                resetTimer(false);
                break;
            case InQueue:
                resetTimer(false);
                break;
            case GameReady:
                startTimer(countDownTime);
                break;
            case InGame:
                resetTimer(true);
        }
    }

    private void resetTimer(boolean isGame){
        this.counter = 0;
        if(isGame){
            countDownIndicator.setProgress(100);
        }
        else{
            countDownIndicator.setProgress(0);
        }

        if(fiveSecondsWonder != null) {
            Platform.runLater(() -> {
                fiveSecondsWonder.stop();
                fiveSecondsWonder = null;
            });
        }
    }

    private void willDisappear(){
        resetTimer(false);
    }

    private void startTimer(double countDownTime){
        fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            this.counter = this.counter + 0.1;
            countDownIndicator.setProgress(this.counter/countDownTime * 100);
        }));
        fiveSecondsWonder.setCycleCount((int) countDownTime * 10);
        fiveSecondsWonder.play();
    }
}
