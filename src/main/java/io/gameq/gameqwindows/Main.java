package io.gameq.gameqwindows;

import io.gameq.gameqwindows.ConnectionHandler.CallbackGeneral;
import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.GameDetector.DotaDetector;
import io.gameq.gameqwindows.GameDetector.GameDetector;
import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;
import io.gameq.gameqwindows.ViewControllers.LoginView.LoginViewController;
import io.gameq.gameqwindows.ViewControllers.MainView.MainViewController;
import io.gameq.gameqwindows.ViewControllers.SignUpView.SignUpViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jdk.nashorn.internal.ir.Symbol;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {


    private boolean firstTime;
    private TrayIcon trayIcon;

    private GameDetector detector = null;
    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 500.0;
    private final double MINIMUM_WINDOW_HEIGHT = 700.0;
    private Timer timer = null;
    private String userName = "";
    private Status status = Status.Offline;
    private Game game = Game.NoGame;
    private MainViewController mainView = null;



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

            createTrayIcon(stage);
            firstTime = true;
            Platform.setImplicitExit(false);


            //TODO Login without internet

            ConnectionHandler.loginWithRememberedDetails((success, error) -> {
                if (success) {
                    Platform.runLater(this::gotoMainView);
                    Platform.runLater(primaryStage::show);
                    didLogin();
                    System.out.println("login success");
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
        Platform.runLater(() -> mainView.updateStatus(game.NoGame, Status.Online));
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

        if(detector != null && this.game != Game.NoGame) {detector.stopDetection();}
        timer.cancel();
        timer.purge();
        setUserName("");
    }

    public void gotoSignUp(){
        Platform.runLater(this::gotoSignUpView);
    }

    public void userLogout(){
        Platform.runLater(this::gotoLoginView);
        didLogOut();
        ConnectionHandler.logout((success, error) -> {
            if (success) {
                System.out.println("logout Success");
            } else {
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
            this.mainView = (MainViewController) replaceSceneContent
                    ("/ViewControllers/MainView/MainView.fxml");
            this.mainView.setApp(this);
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
            scene = new Scene(page, 500, 700);
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

    public void updateStatus(Status newStatus){
        this.status = newStatus;
        ConnectionHandler.setStatus((success, error) -> {
            if(success){
                System.out.println("successfully updated status");
            }
            else{
                System.out.println(error);
            }
        }, Encoding.getIntFromGame(this.game), Encoding.getIntFromStatus(status));

        mainView.updateStatus(this.game, this.status);
    }


    public void createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            // get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();
            // load an image
            java.awt.Image image = null;
            try {
                URL url = new URL("http://www.digitalphotoartistry.com/rose1.jpg");
                image = ImageIO.read(url);
            } catch (IOException ex) {
                System.out.println(ex);
            }


            stage.setOnCloseRequest(t -> hide(stage));
            // create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = e -> System.exit(0);

            ActionListener showListener = e -> Platform.runLater(() -> stage.show());
            // create a popup menu
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);
            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon(image, "Title", popup);
            // set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            // ...
            // add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
            // ...
        }
    }

    public void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("Some message.",
                    "Some other message.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
                showProgramIsMinimizedMsg();
            } else {
                System.exit(0);
            }
        });
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}