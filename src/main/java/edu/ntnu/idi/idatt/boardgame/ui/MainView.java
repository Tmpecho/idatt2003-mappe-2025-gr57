package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.view.CluedoView;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnlController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.JsonSnlGameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view.SnlView;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code MainView} class serves as the primary container for the application, featuring a
 * sidebar with game selection and placeholders for save/load functionality.
 */
public final class MainView {

  private static final Logger logger = LoggerFactory.getLogger(MainView.class);

  private final BorderPane root;
  private final StackPane contentWrapper;
  private GameController<?> currentController;
  private Button saveGameButton;
  private Button loadGameButton;

  /**
   * Constructs the main view of the application. Initializes the root layout and loads the main
   * menu sidebar.
   */
  public MainView() {
    root = new BorderPane();

    contentWrapper = new StackPane();
    contentWrapper.setPadding(new Insets(10));
    // Set a default message or view in the contentWrapper
    contentWrapper.getChildren().add(new Label("Welcome! Click 'Menu' to start."));
    root.setCenter(contentWrapper);

    loadMenu();
  }

  /**
   * Returns the root {@link BorderPane} of this view.
   *
   * @return The root pane.
   */
  public BorderPane getRoot() {
    return root;
  }

  private void loadMenu() {
    VBox sidebar = new VBox(15);
    sidebar.setPadding(new Insets(10));
    sidebar.setAlignment(javafx.geometry.Pos.TOP_CENTER);
    sidebar.setStyle("-fx-background-color: #336699;");

    // Create the new Menu button
    Button menuButton = new Button("Menu");
    menuButton.setOnAction(e -> showChooseGameView());
    menuButton.setMaxWidth(Double.MAX_VALUE);

    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    saveGameButton = buildSaveButton();
    loadGameButton = buildLoadButton();
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);

    Button exitButton = new Button("Exit");
    exitButton.setOnAction(e -> Platform.exit());
    exitButton.setMaxWidth(Double.MAX_VALUE);
    saveGameButton.setMaxWidth(Double.MAX_VALUE);
    loadGameButton.setMaxWidth(Double.MAX_VALUE);

    sidebar
        .getChildren()
        .addAll(menuButton, spacer, saveGameButton, loadGameButton, exitButton);
    root.setLeft(sidebar);
  }

  /**
   * Shows a placeholder for the "Choose Game" view in the content wrapper.
   */
  private void showChooseGameView() {
    // Placeholder for now. This will be replaced with the actual ChooseGameView in Phase 2.
    Label chooseGamePlaceholder = new Label("Choose Game View Placeholder\n\n"
        + " (This will show game options like Snakes & Ladders and Cluedo)");
    chooseGamePlaceholder.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
    chooseGamePlaceholder.setWrapText(true);
    contentWrapper.getChildren().setAll(chooseGamePlaceholder);

    if (currentController != null) {
      // Consider if we want to clear currentController here or prompt the user
      // For now, let's assume we might be navigating away from an active game,
      // but the game state itself isn't cleared until a new game starts.
      // Save/Load buttons should reflect the state of `currentController`.
      // However, if we are at the menu, save/load for a *new* game doesn't make sense yet.
    }
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(false);
  }


  private Button buildSaveButton() {
    Button button = new Button("Save game");
    // button.setDisable(true); // Initial state set in loadMenu
    button.setOnAction(
        e -> {
          if (currentController == null) {
            logger.warn("No game loaded to save.");
            LoggingNotification.error("No game loaded", "Cannot save game.");
            return;
          }
          FileChooser chooser = new FileChooser();
          chooser.setTitle("Save Game State");
          File dir = new File("saves");
          if (!dir.exists()) {
            boolean success = dir.mkdirs();

            if (!success) {
              logger.error("Failed to create saves directory. Cannot save game.");
              LoggingNotification.error("Failed to create saves directory", "Cannot save game.");
              return;
            }
          }
          chooser.setInitialDirectory(dir);
          chooser.setInitialFileName("game_state.json");
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
          Stage stage = getStage();
          if (stage == null) {
            logger.error("Could not get the stage to show save dialog.");
            LoggingNotification.error("Failed to save game", "Could not get the stage.");
            return;
          }
          File file = chooser.showSaveDialog(stage);
          if (file == null) {
            logger.info("Save cancelled. No file selected.");
            LoggingNotification.info("Save cancelled", "No file selected.");
            return;
          }
          currentController.saveGameState(file.getPath());
        });
    return button;
  }

  private Button buildLoadButton() {
    Button button = new Button("Load game");
    // button.setDisable(true); // Initial state set in loadMenu
    button.setOnAction(
        e -> {
          FileChooser chooser = new FileChooser();
          chooser.setTitle("Load Game State");
          File dir = new File("saves");
          if (!dir.exists()) {
            logger.error("Saves directory does not exist. Cannot load game.");
            LoggingNotification.error("Failed to load game", "Saves directory does not exist.");
            return;
          }
          chooser.setInitialDirectory(dir);
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
          Stage stage = getStage();
          if (stage == null) {
            logger.error("Could not get the stage to show load dialog.");
            LoggingNotification.error("Failed to load game", "Could not get the stage.");
            return;
          }
          File file = chooser.showOpenDialog(stage);
          if (file == null) {
            logger.info("Load cancelled. No file selected.");
            return;
          }

          boolean wasGameActive = currentController != null;

          if (currentController == null) {
            // Attempt to load Snakes and Ladders by default if no game is active
            logger.warn(
                "No game context for loading. Attempting to load as Snakes and Ladders.");
            loadSnakesAndLaddersForLoading(); // A specific method to set up for loading
          }

          if (currentController == null) {
            logger.error("Failed to initialize a game controller for loading.");
            LoggingNotification.error("Failed to load game", "No game controller available.");
            return;
          }

          currentController.loadGameState(file.getPath());
          saveGameButton.setDisable(false);
          loadGameButton.setDisable(false);


        });
    return button;
  }

  /**
   * Helper method to initialize Snakes and Ladders specifically for the purpose of loading a save
   * file, without necessarily making it the "active" game being played from scratch. This method
   * sets up the controller and view, then the loadGameState will be called on it.
   */
  private void loadSnakesAndLaddersForLoading() {
    Path savesDir = Path.of("saves");
    try {
      Files.createDirectories(savesDir);
    } catch (IOException e) {
      logger.error("Failed to create saves directory: {}", e.getMessage());
      LoggingNotification.error("Failed to create saves directory", "Cannot load game.");
      this.currentController = null; // Ensure controller is null if setup fails
      return;
    }

    var repo = new JsonSnlGameStateRepository();
    SnlController controller = new SnlController(2,
        repo); // Default to 2 players for loading context

    this.currentController = controller;
    SnlView view = new SnlView(controller); // View is created, observers will be notified by load
    contentWrapper.getChildren().setAll(view.getRoot());
    // Save/load buttons will be managed by the calling load method
  }

  private void loadSnakesAndLadders() {
    Path savesDir = Path.of("saves");
    try {
      Files.createDirectories(savesDir);
    } catch (IOException e) {
      logger.error("Failed to create saves directory: {}", e.getMessage());
      LoggingNotification.error("Failed to create saves directory", "Cannot load game.");
      return;
    }

    var repo = new JsonSnlGameStateRepository();
    SnlController controller = new SnlController(2, repo); // Default to 2 players for now

    this.currentController = controller;
    SnlView view = new SnlView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());

    saveGameButton.setDisable(false);
    loadGameButton.setDisable(false);
  }

  private void loadCluedo() {
    int numberOfPlayers = 6; // Default to 6 players for now
    try {
      CluedoController cluedoController = new CluedoController(numberOfPlayers);
      this.currentController = cluedoController;

      CluedoView view = new CluedoView(cluedoController);
      contentWrapper.getChildren().setAll(view.getRoot());

      saveGameButton.setDisable(true);
      loadGameButton.setDisable(true);

    } catch (IllegalArgumentException ex) {
      logger.error("Failed to load Cluedo: {}", ex.getMessage());
      contentWrapper.getChildren().setAll(new Label("Error loading Cluedo: " + ex.getMessage()));
      saveGameButton.setDisable(true);
      loadGameButton.setDisable(true);
      this.currentController = null;
    } catch (Exception ex) {
      logger.error("An unexpected error occurred while loading Cluedo: {}", ex.getMessage(), ex);
      contentWrapper.getChildren().setAll(new Label("Unexpected error loading Cluedo."));
      saveGameButton.setDisable(true);
      loadGameButton.setDisable(true);
      this.currentController = null;
    }
  }

  private Stage getStage() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      return (Stage) root.getScene().getWindow();
    }
    return null;
  }
}
