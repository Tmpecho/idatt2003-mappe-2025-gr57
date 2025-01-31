package edu.idi.idatt;

import javafx.application.Application;
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
    TileGrid tileGrid = new TileGrid(10, 10, 50);

    VBox root = new VBox(10, label, tileGrid);
    Scene scene = new Scene(root, 400, 300);
    stage.setScene(scene);
    stage.setTitle("JavaFX Maven App");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}