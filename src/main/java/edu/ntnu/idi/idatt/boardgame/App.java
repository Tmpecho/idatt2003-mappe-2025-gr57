package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.controller.GameController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    GameController gameController = new GameController();

    VBox root =
        new VBox(
            gameController.getGameBoard(),
            gameController.getRollDiceButton(),
            gameController.getLogLabel());
    Scene scene = new Scene(root, 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
