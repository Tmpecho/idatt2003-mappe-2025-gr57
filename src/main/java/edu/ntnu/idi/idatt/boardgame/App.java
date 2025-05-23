package edu.ntnu.idi.idatt.boardgame;

import atlantafx.base.theme.PrimerLight;
import edu.ntnu.idi.idatt.boardgame.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main application class for the board game hub. Initializes and shows the primary JavaFX stage
 * and scene.
 */
public final class App extends Application {

  /**
   * The main method, which launches the JavaFX application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    launch();
  }

  /**
   * The main entry point for all JavaFX applications. This method is called after the init method
   * has returned, and after the system is ready for the application to begin running.
   *
   * @param stage the primary stage for this application, onto which the application scene can be
   *     set.
   */
  @Override
  public void start(Stage stage) {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    MainView mainView = new MainView();
    Scene scene = new Scene(mainView.getRoot(), 900, 800);
    stage.setScene(scene);
    stage.setTitle("Board Games Hub");
    stage.setFullScreen(true);
    stage.setOnCloseRequest(evt -> System.exit(0));
    stage.show();
  }
}
