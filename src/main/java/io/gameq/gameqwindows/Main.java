package io.gameq.gameqwindows;

import io.gameq.gameqwindows.ConnectionHandler.CallbackGeneral;
import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.GameDetector.DotaDetector;
import io.gameq.gameqwindows.GameDetector.GameDetector;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;
import io.gameq.gameqwindows.ViewControllers.LoginView.LoginViewController;
import io.gameq.gameqwindows.ViewControllers.MainView.MainViewController;
import io.gameq.gameqwindows.ViewControllers.SignUpView.SignUpViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jdk.nashorn.internal.ir.Symbol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    private GameDetector detector = new GameDetector();
    private Game game = null;
    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 500.0;
    private final double MINIMUM_WINDOW_HEIGHT = 600.0;
    private Timer timer = null;
    private String userName = "";



    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("GameQ");
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            stage.setMaxWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMaxHeight(MINIMUM_WINDOW_HEIGHT);
            stage.setResizable(false);

            ConnectionHandler.loginWithRememberedDetails((success, error) -> {
                if (success) {
                    Platform.runLater(this::gotoMainView);
                    Platform.runLater(primaryStage::show);
                }
                else {
                    Platform.runLater(this::gotoLoginView);
                    Platform.runLater(primaryStage::show);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void didLogin(){
        detector.updateStatus(Status.Online);
        this.timer = new Timer();
        timer.schedule(
                new TimerTask() {

                    @Override
                    public void run() {
                        update();
                    }
                }, 0, 1000);

//            self.gameItem.title = Encoding.getStringFromGame(GameDetector.game)
//            self.gameItem.enabled = false
//            self.statusItem.title = Encoding.getStringFromGameStatus(GameDetector.game, status: GameDetector.status)
//            self.statusItem.enabled = false
//            self.emailItem.title = ConnectionHandler.loadEmail()!
//                    self.emailItem.enabled = false
//            self.menu.removeAllItems()
//            self.menu.addItem(self.emailItem)
//            self.menu.addItem(self.gameItem)
//            self.menu.addItem(self.statusItem)
//            self.menu.addItem(NSMenuItem.separatorItem())
//            self.menu.addItem(self.preferencesItem)
//            self.menu.addItem(self.quitItem)
    }

    private void didLogOut(){
//        menu.removeAllItems()
//        menu.addItem(loginItem)
//        menu.addItem(quitItem)
        detector.stopDetection();
        timer.cancel();
        timer.purge();
        setUserName("");
//        ConnectionHandler.logout({ (success:Bool, err:String?) in})
    }

    public void gotoSignUp(){
        Platform.runLater(this::gotoSignUpView);
    }

    public void userLogout(){
        Platform.runLater(this::gotoLoginView);
        //TODO add later
        // didLogOut();
        ConnectionHandler.logout((success, error) -> {
            if(success){
                System.out.println("logout Success");
            }
            else{
                System.out.println("logout failed");
                System.out.println(error);
            }
        });
    }

    public void userBackToLogin(){
        Platform.runLater(this::gotoLoginView);
    }

    public void gotoMainView() {
        try {
            MainViewController mainView = (MainViewController) replaceSceneContent
                    ("/ViewControllers/MainView/MainView.fxml");
            mainView.setApp(this);
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }
    private void gotoLoginView() {
        try {
            LoginViewController login = (LoginViewController) replaceSceneContent
                    ("/ViewControllers/LoginView/LoginView.fxml");
            login.setApp(this);
        } catch (Exception ex) {
            //  ex.printStackTrace();
        }
    }
    private void gotoSignUpView() {
        try {
            SignUpViewController signUp = (SignUpViewController) replaceSceneContent
                    ("/ViewControllers/SignUpView/SignUpView.fxml");
            signUp.setApp(this);
        } catch (Exception ex) {;
            /* ex.printStackTrace(); */
        }
    }


    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        VBox page = null;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = (VBox) loader.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Scene scene = null;
        if (page != null) {
            scene = new Scene(page, 500, 600);
        }
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }


    // Lots of games to add
    private void update(){

        Game newGame = null;

        try {
            String line;
            Process p = Runtime.getRuntime().exec
                    (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if(line.contains("dota.exe")){
                    newGame = Game.Dota2;
                }
                else{
                    newGame = Game.NoGame;
                }
            }
            input.close();
        } catch (Exception err) {
            // err.printStackTrace();
        }

        if(game != newGame){
            detector = new DotaDetector();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}