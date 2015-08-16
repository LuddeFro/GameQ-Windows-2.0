package io.gameq.gameqwindows.ViewControllers.SignUpView;

/**
 * Created by fabianwikstrom on 7/14/2015.
 */

import io.gameq.gameqwindows.ConnectionHandler.CallbackGeneral;
import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpViewController extends VBox implements Initializable {

    @FXML TextField emailField;
    @FXML PasswordField pwField1;
    @FXML PasswordField pwField2;
    @FXML Button signUpButton;
    @FXML Button backButton;
    @FXML Label statusLabel;
    @FXML ImageView exitButton;
    @FXML ImageView minButton;

    private Main application;

    public void setApp(Main application) {
        this.application = application;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void exitPressed(){
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // do what you have to do
        Platform.runLater(stage::close);
    }

    public void minPressed(){
        Stage stage = (Stage) minButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void backPressed() {
        if (application == null) {
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            //errorMessage.setText("Hello " + userId.getText());
        } else {
            application.userBackToLogin();
        }
    }

    public void onEnter(){
        processSignUp();
    }

    public void processSignUp() {
        if (application == null) {
        }
        //TODO check for email password error
        else if (true) {
            Platform.runLater(() -> signUpButton.setDisable(true));
            Platform.runLater(() -> backButton.setDisable(true));
            Platform.runLater(() -> statusLabel.setText("Creating Account..."));
            ConnectionHandler.register((success, error) -> {
                if (success) {
                    Platform.runLater(() -> signUpButton.setDisable(false));
                    Platform.runLater(() -> backButton.setDisable(false));
                    Platform.runLater(() -> statusLabel.setText("Success!"));
                    Platform.runLater(application::gotoMainView);
                    //TODO ADd later
                    //didLogin();
                    application.setUserName(emailField.getText());
                } else {
                    Platform.runLater(() -> signUpButton.setDisable(false));
                    Platform.runLater(() -> backButton.setDisable(false));
                    Platform.runLater(() -> statusLabel.setText(error));
                }
            }, emailField.getText(), pwField1.getText());

            ConnectionHandler.login((success, error) -> {

            }, "asd", "asd");


        } else {
            //TODO format error message
        }
    }
}
