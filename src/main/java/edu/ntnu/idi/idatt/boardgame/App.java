package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.controller.SnakesAndLaddersController; // Updated import
import edu.ntnu.idi.idatt.boardgame.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    SnakesAndLaddersController gameController = new SnakesAndLaddersController(2); // Updated
    GameView gameView = new GameView(gameController, gameController.getGameBoard());

    Scene scene = new Scene(gameView.getRoot(), 700, 800);
    stage.setScene(scene);
    stage.setTitle("Snakes and Ladders"); // Updated title for clarity
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}