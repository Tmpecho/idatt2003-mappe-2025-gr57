package edu.ntnu.idi.idatt.boardgame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    Game game = new Game();

    AnchorPane root = new AnchorPane(game.getGameBoard());
    Scene scene = new Scene(root, 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
