package io.gameq.gameqwindows.ViewControllers.LoginView;

import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.Main;
import io.gameq.gameqwindows.Structs.Status;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 7/14/2015.
 */

public class LoginViewController extends VBox implements Initializable {

    @FXML Label statusLabel;
    @FXML PasswordField passwordField;
    @FXML TextField emailField;
    @FXML Button loginButton;
    @FXML Button signUp;

    private Main application;

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void processLogin() {
        if (application == null){
           System.out.println("Error no application");
        }
        //TODO check for email password error
        else if(true) {
            Platform.runLater(() -> loginButton.setDisable(true));
            Platform.runLater(() -> signUp.setDisable(true));
            Platform.runLater(() -> statusLabel.setText("Signing in..."));
            ConnectionHandler.login((success, error) -> {
                if (success) {
                    //didLogin();
                    Platform.runLater(() ->  statusLabel.setText("Success!"));
                    Platform.runLater(() -> loginButton.setDisable(false));
                    Platform.runLater(() -> signUp.setDisable(false));
                    Platform.runLater(application::gotoMainView);
                } else {
                    Platform.runLater(() -> statusLabel.setText(error));
                    Platform.runLater(() -> loginButton.setDisable(false));
                    Platform.runLater(() -> signUp.setDisable(false));
                }

            }, emailField.getText(), passwordField.getText());
        }
        else{
            //TODO format error message
        }
    }

    public void gotoSignUp(){

        if (application == null){
        } else {
            application.gotoSignUp();
        }
    }
}
