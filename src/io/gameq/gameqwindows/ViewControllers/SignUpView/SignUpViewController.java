package io.gameq.gameqwindows.ViewControllers.SignUpView;

/**
 * Created by fabianwikstrom on 7/14/2015.
 */

import io.gameq.gameqwindows.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpViewController extends VBox implements Initializable {

    @FXML
    Button signUpButton;

    @FXML
    Button backButton;

    private Main application;

    public void setApp(Main application){
        this.application = application;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void backPressed(){
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            //errorMessage.setText("Hello " + userId.getText());
        } else {
            application.userBackToLogin();
            }
        }

    public void processSignUp() {
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            //errorMessage.setText("Hello " + userId.getText());
        } else {
            if (!application.userSignUp("bajs", "bajs")){
                System.out.println("Not yet implemented");
            }
        }
    }

}
