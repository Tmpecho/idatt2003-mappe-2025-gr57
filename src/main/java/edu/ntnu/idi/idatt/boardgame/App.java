package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    GameController gameController = new GameController();

    GameView gameView = new GameView(gameController, gameController.getGameBoard());

    gameController.setGameView(gameView);

    Scene scene = new Scene(gameView.getRoot(), 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}