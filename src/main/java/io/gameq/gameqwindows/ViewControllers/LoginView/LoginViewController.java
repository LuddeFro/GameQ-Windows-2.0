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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

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
    @FXML Button forgotButton;
    @FXML ImageView lockImage;
    @FXML HBox textHolder;
    @FXML HBox forgotBox;
    @FXML Line forgotLine;
    @FXML ImageView exitButton;
    @FXML ImageView minButton;



    private Main application;
    private boolean isForgot = false;

    public void setApp(Main application){
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

    public void onEnter(){
        processLogin();
    }

    public void processForgot(){
        Platform.runLater(() -> {
            isForgot = true;
            forgotButton.setDisable(true);
            forgotButton.setVisible(false);
            passwordField.setVisible(false);
            passwordField.setDisable(true);
            loginButton.setText("Submit");
            signUp.setText("Back");
            lockImage.setVisible(false);
            textHolder.setOpacity(0);
            forgotBox.setOpacity(1);
            forgotLine.setVisible(false);
            forgotBox.setVisible(true);
                          });
    }

    public void processLogin() {
        if(!isForgot){
            if (application == null){
                System.out.println("Error no application");
            }
            //TODO check for email password error
            else if(true) {
                Platform.runLater(() -> loginButton.setDisable(true));
                Platform.runLater(() -> signUp.setDisable(true));
                Platform.runLater(() -> exitButton.setDisable(true));
                Platform.runLater(() -> minButton.setDisable(true));
                Platform.runLater(() -> forgotButton.setDisable(true));
                Platform.runLater(() -> statusLabel.setText("Signing in..."));
                ConnectionHandler.login((success, error) -> {
                    if (success) {
                        application.setUserName(emailField.getText());
                        Platform.runLater(() -> statusLabel.setText("Success!"));
                        Platform.runLater(() -> loginButton.setDisable(false));
                        Platform.runLater(() -> signUp.setDisable(false));
                        Platform.runLater(() -> forgotButton.setDisable(false));
                        Platform.runLater(() -> exitButton.setDisable(false));
                        Platform.runLater(() -> minButton.setDisable(false));
                        Platform.runLater(application::gotoMainView);
                        Platform.runLater(application::didLogin);
                    } else {
                        Platform.runLater(() -> statusLabel.setText(error));
                        Platform.runLater(() -> loginButton.setDisable(false));
                        Platform.runLater(() -> signUp.setDisable(false));
                        Platform.runLater(() -> forgotButton.setDisable(false));
                        Platform.runLater(() -> exitButton.setDisable(false));
                        Platform.runLater(() -> minButton.setDisable(false));
                    }
                }, emailField.getText(), passwordField.getText());
            }
            else{
                //TODO format error message
            }}
        else{
            //TODO check for email password error
             if(true) {
                Platform.runLater(() -> loginButton.setDisable(true));
                Platform.runLater(() -> signUp.setDisable(true));
                Platform.runLater(() -> forgotButton.setDisable(true));
                 Platform.runLater(() -> exitButton.setDisable(true));
                 Platform.runLater(() -> minButton.setDisable(true));
                Platform.runLater(() -> statusLabel.setText("Submitting..."));
                ConnectionHandler.submitForgotPassword((success, error) -> {
                    if (success) {
                        Platform.runLater(() -> statusLabel.setText("Success!"));
                        Platform.runLater(() -> loginButton.setDisable(false));
                        Platform.runLater(() -> signUp.setDisable(false));
                        Platform.runLater(() -> forgotButton.setDisable(false));
                        Platform.runLater(() -> exitButton.setDisable(false));
                        Platform.runLater(() -> minButton.setDisable(false));

                    } else {
                        Platform.runLater(() -> statusLabel.setText(error));
                        Platform.runLater(() -> loginButton.setDisable(false));
                        Platform.runLater(() -> signUp.setDisable(false));
                        Platform.runLater(() -> forgotButton.setDisable(false));
                        Platform.runLater(() -> exitButton.setDisable(false));
                        Platform.runLater(() -> minButton.setDisable(false));
                    }
                }, emailField.getText());
            }
            else{
                 //TODO Wrong format
             }
        }
    }

    public void gotoSignUp(){
        Platform.runLater(() -> {
            if(!isForgot) {
                if (application == null) {

                } else {
                    application.gotoSignUp();
                }
            }
            else{
                isForgot = false;
                loginButton.setText("Login");
                signUp.setText("Sign Up");
                forgotButton.setVisible(true);
                forgotButton.setDisable(false);
                passwordField.setDisable(false);
                passwordField.setVisible(true);
                loginButton.setDisable(false);
                forgotBox.setOpacity(0);
                forgotLine.setVisible(true);
                textHolder.setVisible(true);
                lockImage.setVisible(true);
                textHolder.setOpacity(1);
            }
        });
    }
}
