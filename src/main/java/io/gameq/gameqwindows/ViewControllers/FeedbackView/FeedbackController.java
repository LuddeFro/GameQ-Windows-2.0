package io.gameq.gameqwindows.ViewControllers.FeedbackView;

import io.gameq.gameqwindows.ConnectionHandler.CallbackGeneral;
import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.Encoding;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 8/12/2015.
 */
public class FeedbackController extends VBox implements Initializable {

    @FXML TextArea feedbackField;
    @FXML Button submitButton;
    @FXML RadioButton didButton;
    @FXML RadioButton didNotButton;
    @FXML RadioButton generalButton;
    @FXML Label statusField;
    @FXML ImageView exitButton;


    private Main application;

    public void exitPressed(){
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // do what you have to do
        Platform.runLater(stage::close);
    }

    public void setApp(Main application){
        this.application = application;
    }

    public void generalPressed(){
        didButton.setSelected(false);
        didNotButton.setSelected(false);
    }

    public void didPressed(){
        generalButton.setSelected(false);
        didNotButton.setSelected(false);
    }

    public void didNotPressed(){
        generalButton.setSelected(false);
        didButton.setSelected(false);
    }

    public void submitPressed(){
        Platform.runLater(() -> submitButton.setDisable(true));
        Platform.runLater(() -> exitButton.setDisable(true));
        Platform.runLater(() -> statusField.setText("Submitting Feedback"));

        if(generalButton.isSelected()){
            ConnectionHandler.submitFeedback((success, error) -> {
                if(success){
                    Platform.runLater(() -> this.statusField.setText("Success, thank you!"));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                    Platform.runLater(() -> feedbackField.setText(""));
                }
                else{
                    Platform.runLater(() -> this.statusField.setText(error));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                }
            },feedbackField.getText());
        }

        //did not recieve notif
        else if(didNotButton.isSelected() && application.fileToString() != null){
            ConnectionHandler.submitCSV((success1, error1) -> {
                if(success1){
                    Platform.runLater(() -> this.statusField.setText("Success, thank you!"));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                    Platform.runLater(() -> feedbackField.setText(""));
                }
                else{
                    Platform.runLater(() -> this.statusField.setText(error1));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                }

            }, application.fileToString(), Encoding.getIntFromGame(application.getGame()),4);

            ConnectionHandler.submitFeedback((success, error) -> {
            }, feedbackField.getText());
        }

        //got wrong notif
        else if(didButton.isSelected() && application.fileToString() != null){
            ConnectionHandler.submitCSV((success1, error1) -> {
                if(success1){
                    Platform.runLater(() -> this.statusField.setText("Success, thank you!"));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                    Platform.runLater(() -> feedbackField.setText(""));
                }
                else{
                    Platform.runLater(() -> this.statusField.setText(error1));
                    Platform.runLater(() -> this.submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                }

            }, application.getDetector().fileToString(), Encoding.getIntFromGame(application.getGame()),5);

            ConnectionHandler.submitFeedback((success, error) -> {
            }, feedbackField.getText());
        }

        else{
            Platform.runLater(() -> submitButton.setDisable(false));
            Platform.runLater(() -> exitButton.setDisable(false));
            Platform.runLater(() -> statusField.setText("thank you!"));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generalButton.setSelected(true);
    }
}
