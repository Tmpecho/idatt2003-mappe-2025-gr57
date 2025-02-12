package edu.idi.idatt;

import edu.idi.idatt.model.Dice;
import edu.idi.idatt.model.GameBoard;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    int numberOfDice = 2;
    Dice dice = new Dice(numberOfDice);

    Label label = new Label("Threw " + numberOfDice + " dice and got " + dice.roll() + ".");
    GameBoard gameBoard = new GameBoard(2);
    gameBoard.setPadding(new Insets(10));

    VBox root = new VBox(10, label, gameBoard);
    Scene scene = new Scene(root, 700, 800);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
