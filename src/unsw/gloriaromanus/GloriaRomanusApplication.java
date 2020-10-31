package unsw.gloriaromanus;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class GloriaRomanusApplication extends Application {

  private Stage window;

  private static GloriaRomanusController controller;

  @Override
  public void start(Stage stage) throws IOException {
    // set up the stage
    stage.setTitle("Gloria Romanus");
    stage.setWidth(800);
    stage.setHeight(700);

    // Set up main menu
    window = stage;

    // set up the scene
    FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
    Parent root = loader.load();
    controller = loader.getController();
    Scene game = new Scene(root);

    // Label mainMenuMsg = new Label ("Welcome to Gloria Romanus!");
    Button startGame = new Button("Start Game");
    startGame.setOnAction(e-> window.setScene(game));
    
    FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
    Parent menuRoot = menuLoader.load();
    Scene menu = new Scene(menuRoot);
    stage.setScene(menu);

    /*
    StackPane mainMenuLayout = new StackPane();
    mainMenuLayout.getChildren().add(startGame);
    Scene mainMenu  = new Scene(mainMenuLayout, 800, 700);
    

    stage.setScene(mainMenu);
    */
    stage.show();

  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {
    controller.terminate();
  }

  /**
   * Opens and runs application.
   *
   * @param args arguments passed to this application
   */
  public static void main(String[] args) {

    Application.launch(args);
  }
}