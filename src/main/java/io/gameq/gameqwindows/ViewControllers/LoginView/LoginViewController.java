package io.gameq.gameqwindows.ViewControllers.LoginView;

import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 7/14/2015.
 */
public class LoginViewController extends VBox implements Initializable {

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
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            //errorMessage.setText("Hello " + userId.getText());
        } else {
            if (!application.userLogin("bajs", "Bajs")){
                System.out.println("Incorrect Shit");
            }
        }
    }

    public void gotoSignUp(){

//        if (application == null){
//            // We are running in isolated FXML, possibly in Scene Builder.
//            // NO-OP.
//            //errorMessage.setText("Hello " + userId.getText());
//        } else {
//            application.gotoSignUp();
//        }
    }
}
