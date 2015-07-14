package io.gameq.gameqwindows.ViewControllers.LoginView;

import io.gameq.gameqwindows.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 7/14/2015.
 */
public class LoginViewController extends VBox implements Initializable {

    @FXML
    Button login;

    private Main application;


    public void setApp(Main application){
        this.application = application;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void processLogin() {
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            //errorMessage.setText("Hello " + userId.getText());
        } else {
            if (!application.userLogging("bajs", "Bajs")){
                System.out.println("Incorrect Shit");
            }
        }
    }

}
