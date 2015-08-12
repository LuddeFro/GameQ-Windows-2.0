package io.gameq.gameqwindows.ViewControllers.SettingsView;

import io.gameq.gameqwindows.Main;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fabianwikstrom on 8/12/2015.
 */
public class SettingsController extends VBox implements Initializable {

    private Main application;

    public void setApp(Main application){
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
