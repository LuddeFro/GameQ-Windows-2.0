package io.gameq.gameqwindows;


import io.gameq.gameqwindows.GameDetector.DotaDetector;
import io.gameq.gameqwindows.GameDetector.GameDetector;
import io.gameq.gameqwindows.Structs.Game;
import io.gameq.gameqwindows.ViewControllers.LoginView.LoginViewController;
import io.gameq.gameqwindows.ViewControllers.MainView.MainViewController;
import io.gameq.gameqwindows.ViewControllers.SignUpView.SignUpViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main extends Application {

    private GameDetector detector = null;
    private Game game = null;
    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 500.0;
    private final double MINIMUM_WINDOW_HEIGHT = 500.0;



    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("GameQ");
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            gotoLoginView();
            primaryStage.show();
        } catch (Exception ex) {
           System.out.println("somth went wrong");
        }


//        new Timer().schedule(
//                new TimerTask() {
//
//                    @Override
//                    public void run() {
//                        update();
//                    }
//                }, 0, 1000);
    }

    public boolean userLogin(String userId, String password){
       //add connection login
        if (true) {
            gotoMainView();
            return true;
        } else {
            return false;
        }
    }
    public boolean userSignUp(String userId, String password){
        //add connection SignUp
        if (true) {
            gotoMainView();
            return true;
        } else {
            return false;
        }
    }
    public void gotoSignUp(){
        gotoSignUpView();
    }
    public void userLogout(){
        gotoLoginView();
    }
    public void userBackToLogin(){
        gotoLoginView();
    }
    private void gotoMainView() {
        try {
            MainViewController mainView = (MainViewController) replaceSceneContent("ViewControllers/MainView/MainView.fxml");
            mainView.setApp(this);
        } catch (Exception ex) {
           System.out.println("what the fuck happened");
        }
    }
    private void gotoLoginView() {
        try {
            LoginViewController login = (LoginViewController) replaceSceneContent("ViewControllers/LoginView/LoginView.fxml");
            login.setApp(this);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    private void gotoSignUpView() {
        try {
            SignUpViewController signUp = (SignUpViewController) replaceSceneContent("ViewControllers/SignUpView/SignUpView.fxml");
            signUp.setApp(this);
        } catch (Exception ex) {;
            System.out.println(ex);
        }
    }


    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        VBox page;
        try {
            page = (VBox) loader.load(in);
        } finally {
            in.close();
        }

        Scene scene = new Scene(page, 500, 500);
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    public static void main(String[] args) {
        launch(args);
    }

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
            err.printStackTrace();
        }

        if(game != newGame){
            detector = new DotaDetector();
        }
    }
}