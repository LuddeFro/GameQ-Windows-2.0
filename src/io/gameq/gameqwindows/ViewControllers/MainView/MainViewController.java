package io.gameq.gameqwindows.ViewControllers.MainView;

import io.gameq.gameqwindows.Main;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController extends VBox implements Initializable {

    public void startButtonClicked(){
        System.out.println("start");
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
        application.userLogout();
    }

    private Main application;

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void processLogout(ActionEvent event) {
        if (application == null){
            // We are running in isolated FXML, possibly in Scene Builder.
            // NO-OP.
            return;
        }

        application.userLogout();
    }
}
