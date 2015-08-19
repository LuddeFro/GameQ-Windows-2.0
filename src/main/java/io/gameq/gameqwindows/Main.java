package io.gameq.gameqwindows;

import io.gameq.gameqwindows.ConnectionHandler.ConnectionHandler;
import io.gameq.gameqwindows.GameDetector.DotaDetector;
import io.gameq.gameqwindows.GameDetector.GameDetector;
import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.Structs.Status;
import io.gameq.gameqwindows.Updater.LoadDll;
import io.gameq.gameqwindows.Updater.WinSparkleDLL;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import javafx.stage.*;
import javafx.scene.image.Image;


public class Main extends Application {



    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.

    private java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
    private java.awt.TrayIcon trayIcon = null;
    private java.awt.MenuItem userItem = new java.awt.MenuItem("user");
    private java.awt.MenuItem statusItem = new java.awt.MenuItem("status");
    private java.awt.MenuItem gameItem = new java.awt.MenuItem("game");
    private java.awt.PopupMenu popup = new java.awt.PopupMenu();

    private GameDetector detector = null;
    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 500.0;
    private final double MINIMUM_WINDOW_HEIGHT = 700.0;
    private Timer timer = null;
    private Status status = Status.Offline;
    private Game game = Game.NoGame;
    private MainViewController mainView = null;
    private String userName = "";
    private java.awt.Font boldFont = new java.awt.Font("Lucida Console", java.awt.Font.BOLD, 20);
    private java.awt.Font regularFont = new java.awt.Font("Lucida Console", java.awt.Font.PLAIN, 20);


    // records relative x and y co-ordinates.
    class Delta { double x, y; }


    @Override
    public void start(Stage primaryStage) {
        try {
            System.loadLibrary("WinSparkle");
            System.loadLibrary("jnetpcap");
            WinSparkleDLL winSparkleDLL = WinSparkleDLL.INSTANCE;
            winSparkleDLL.win_sparkle_set_appcast_url("http://www.gameq.io/app/windows/appcast.xml");
            winSparkleDLL.win_sparkle_set_app_details("GameQ AB", "GameQ Windows", "1.0");
            winSparkleDLL.win_sparkle_set_automatic_check_for_updates(1);
            winSparkleDLL.win_sparkle_set_update_check_interval(3600 * 24);
            winSparkleDLL.win_sparkle_init();
            winSparkleDLL.win_sparkle_check_update_with_ui();
//            //TODO somth with cleanup

            stage = primaryStage;
            stage.setTitle("GameQ");
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            stage.setMaxWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMaxHeight(MINIMUM_WINDOW_HEIGHT);
            stage.setResizable(false);
            Font.loadFont(
                    Main.class.getResource("/fonts/Roboto-Regular.ttf").toExternalForm(),
                    20);

            // stage.initStyle(StageStyle.UTILITY);

            stage.getIcons().addAll(
                    new Image("/images/gq-nb-16.png"),
                    new Image("/images/gq-nb-32.png"),
                    new Image("/images/gq-nb-64.png")
            );

            stage.initStyle(StageStyle.UNDECORATED);

            Platform.setImplicitExit(false);
            // sets up the tray icon (using awt code run on the swing thread).
            javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

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
                    Platform.runLater(this::setLogOutPopUp);
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
        this.status = Status.Online;
        this.game = Game.NoGame;
        Platform.runLater(() -> mainView.updateStatus(this.game, this.status, 0));
        ConnectionHandler.setStatus(((success, error) -> {
            if (success) {
                System.out.println("Updated status to online no game");
            } else {
            }
        }), Encoding.getIntFromGame(this.game), Encoding.getIntFromStatus(this.status));

        popup.removeAll();

        userName = ConnectionHandler.loadEmail();
        userItem.setLabel(userName);
        statusItem.setLabel(Encoding.getStringFromGameStatus(Game.NoGame, Status.Online));
        gameItem.setLabel(Encoding.getStringFromGame(Game.NoGame));

        // if the user selects the default menu item (which includes the app name),
        // show the main app stage.
        java.awt.MenuItem openItem = new java.awt.MenuItem("Open GameQ");
        openItem.addActionListener(event -> Platform.runLater(this::showStage));

        // the convention for tray icons seems to be to set the default icon for opening
        // the application stage in a bold font.
        openItem.setFont(boldFont);

        // if the user selects the default menu item (which includes the app name),
        // show the main app stage.
        java.awt.MenuItem logOutItem = new java.awt.MenuItem("LogOut");
        logOutItem.addActionListener(event -> Platform.runLater(this::userLogout));

        // the convention for tray icons seems to be to set the default icon for opening
        // the application stage in a bold font.
        logOutItem.setFont(boldFont);

        // to really exit the application, the user must go to the system tray icon
        // and select the exit option, this will shutdown JavaFX and remove the
        // tray icon (removing the tray icon will also shut down AWT).
        java.awt.MenuItem exitItem = new java.awt.MenuItem("Quit");
        exitItem.setFont(boldFont);
        exitItem.addActionListener(event -> {
            Platform.exit();
            tray.remove(trayIcon);
            timer.cancel();
            timer.purge();
            System.exit(0);
        });

        // setup the popup menu for the application.
        userItem.setFont(regularFont);
        statusItem.setFont(regularFont);
        gameItem.setFont(regularFont);
        popup.add(userItem);
        popup.add(statusItem);
        popup.add(gameItem);
        popup.addSeparator();
        popup.add(openItem);
        popup.add(logOutItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, 1000);
    }


    private void setLogOutPopUp(){
        popup.removeAll();

        // if the user selects the default menu item (which includes the app name),
        // show the main app stage.
        java.awt.MenuItem openItem = new java.awt.MenuItem("Open GameQ");
        openItem.addActionListener(event -> Platform.runLater(this::showStage));

        // the convention for tray icons seems to be to set the default icon for opening
        // the application stage in a bold font.
        openItem.setFont(boldFont);

        // to really exit the application, the user must go to the system tray icon
        // and select the exit option, this will shutdown JavaFX and remove the
        // tray icon (removing the tray icon will also shut down AWT).
        java.awt.MenuItem exitItem = new java.awt.MenuItem("Quit");
        exitItem.setFont(boldFont);
        exitItem.addActionListener(event -> {
            Platform.exit();
            tray.remove(trayIcon);
        });

        popup.add(openItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
    }


    private void didLogOut(){
        Platform.runLater(this::setLogOutPopUp);
        mainView.willDisappear();
        if(detector != null && this.game != Game.NoGame) {detector.stopDetection();
        }
        timer.cancel();
        timer.purge();
        setUserName("");
    }

    public void gotoSignUp(){
        Platform.runLater(this::gotoSignUpView);
    }

    public void userLogout() {
        Platform.runLater(this::gotoLoginView);
        didLogOut();
        ConnectionHandler.logout((success, error) -> {
            if (success) {
                System.out.println("logout Success");
            } else {
                System.out.println("logout ");
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
        } catch (Exception ex) {
            /* ex.printStackTrace(); */
        }
    }


    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        VBox page = null;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = loader.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Scene scene = null;
        if (page != null) {
            scene = new Scene(page, 500, 700);
        }
        stage.setScene(scene);
        stage.sizeToScene();

        // allow the background to be used to drag the clock around.
        final Delta dragDelta = new Delta();
        if (page != null) {
            page.setOnMousePressed(mouseEvent -> {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = stage.getX() - mouseEvent.getScreenX();
                dragDelta.y = stage.getY() - mouseEvent.getScreenY();
            });
            page.setOnMouseDragged(mouseEvent -> {
                stage.setX(mouseEvent.getScreenX() + dragDelta.x);
                stage.setY(mouseEvent.getScreenY() + dragDelta.y);
            });
        }

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
            }
            if(newGame == null){
                newGame = Game.NoGame;}
            input.close();
        } catch (Exception err) {
            // err.printStackTrace();
        }

        if (game != newGame){
            detector = new DotaDetector();
            this.game = newGame;
            detector.startDetection(this);
        }
    }

    public void updateStatus(Status newStatus) {
        this.status = newStatus;

        Platform.runLater(() -> statusItem.setLabel(Encoding.getStringFromGameStatus(this.game, this.status)));
        Platform.runLater(() -> gameItem.setLabel(Encoding.getStringFromGame(this.game)));

        ConnectionHandler.setStatus((success, error) -> {
            if (success) {
                System.out.println("successfully updated status");
            } else {
                System.out.println(error);
            }
        }, Encoding.getIntFromGame(this.game), Encoding.getIntFromStatus(status));

        double countDown = 10;
        if(detector != null){ countDown = detector.getCountDownLength();}
        final double finalCountDown = countDown;
        Platform.runLater(() -> mainView.updateStatus(this.game, this.status, finalCountDown));
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }


            String iconImageLoc = String.valueOf(this.getClass().getClassLoader().getResource("images/gq-bb-16.jpg"));
            // set up a system tray icon.
            URL imageLoc = new URL(
                    iconImageLoc
            );
            java.awt.Image image = ImageIO.read(imageLoc);
            trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            trayIcon.addActionListener(e -> Platform.runLater(stage::show));

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }


    public Game getGame(){
        return this.game;
    }

    public Status getStatus(){
        return this.status;
    }

    public GameDetector getDetector(){
        return detector;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}