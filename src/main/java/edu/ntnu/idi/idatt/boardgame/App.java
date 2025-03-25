package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.common.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override
  public void start(Stage stage) {
    MainView mainView = new MainView();
    Scene scene = new Scene(mainView.getRoot(), 900, 800);
    stage.setScene(scene);
    stage.setTitle("Board Games Hub");
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
