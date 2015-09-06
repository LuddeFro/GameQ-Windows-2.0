package io.gameq.gameqwindows.ViewControllers.SettingsView;

import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 8/12/2015.
 */
public class SettingsController extends VBox implements Initializable {

    @FXML Label statusLabel;
    @FXML Button submitButton;
    @FXML PasswordField oldpw;
    @FXML PasswordField pw1;
    @FXML PasswordField pw2;
    @FXML ImageView exitButton;


    private Main application;

    public void exitPressed(){
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // do what you have to do
        Platform.runLater(stage::close);
    }

    public void logOutPressed(){
        application.userLogout();
    }

    public void submitButtonPressed(){

        if(!(pw1.getText().equals(pw2.getText())) || pw1.getText().isEmpty()){
            Platform.runLater(()-> statusLabel.setText("Passwords are not the same"));
        }
        else {
            Platform.runLater(()-> statusLabel.setText("changing password"));
            Platform.runLater(()-> submitButton.setDisable(true));
            Platform.runLater(() -> exitButton.setDisable(true));

            ConnectionHandler.updatePassword((success, error) -> {
                if(success){
                    Platform.runLater(()-> statusLabel.setText("Your password has been changed"));
                    Platform.runLater(()-> submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                }
                else{
                    Platform.runLater(()-> statusLabel.setText(error));
                    Platform.runLater(()-> submitButton.setDisable(false));
                    Platform.runLater(() -> exitButton.setDisable(false));
                }

            }, application.getUserName(), pw2.getText(), oldpw.getText());
        }
    }

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
