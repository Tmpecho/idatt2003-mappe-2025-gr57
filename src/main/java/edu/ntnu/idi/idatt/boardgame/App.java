package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.controller.common.GameController;
import edu.ntnu.idi.idatt.boardgame.controller.games.snakesAndLadders.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class App extends Application {
  SnakesAndLaddersController snakesAndLaddersController = new SnakesAndLaddersController(2);
  private final List<GameController> gameControllers = List.of(snakesAndLaddersController);
  GameView gameView = new GameView(gameControllers);

  @Override
  public void start(Stage stage) {
    snakesAndLaddersController.setGameView(gameView);

    snakesAndLaddersController.startGame();

    Scene scene = new Scene(gameView.getRoot(), 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}