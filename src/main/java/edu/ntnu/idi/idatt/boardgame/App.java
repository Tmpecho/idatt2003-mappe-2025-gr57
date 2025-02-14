package edu.ntnu.idi.idatt.boardgame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    Game game = new Game();

    VBox root = new VBox(game.getGameBoard(), game.getRollDiceButton(), game.getLogLabel());
    Scene scene = new Scene(root, 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
