package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.view.CluedoView;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnlController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.JsonSnlGameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view.SnlView;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import edu.ntnu.idi.idatt.boardgame.ui.view.ChooseGameView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
  private String selectedGameType; // To store the selected game type

  /**
   * Constructs the main view of the application. Initializes the root layout and loads the main
   * menu sidebar.
   */
  public MainView() {
    root = new BorderPane();

    contentWrapper = new StackPane();
    contentWrapper.setPadding(new Insets(10));
    // Set a default message or view in the contentWrapper
    Label welcomeLabel = new Label("Welcome! Click 'Menu' to start.");
    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
    StackPane.setAlignment(welcomeLabel, Pos.CENTER); // Center the label in StackPane
    contentWrapper.getChildren().add(welcomeLabel);
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
   * Shows the "Choose Game" view in the content wrapper.
   */
  private void showChooseGameView() {
    ChooseGameView chooseGameView = new ChooseGameView(
        gameType -> { // onGameSelected lambda
          this.selectedGameType = gameType;
          logger.info("Game selected: {}", gameType);
          showPlayerConfigurationView(gameType);
        },
        () -> { // onBack lambda
          logger.info("Back to main menu from ChooseGameView.");
          // Reset contentWrapper to initial state
          Label welcomeLabel = new Label("Welcome! Click 'Menu' to start.");
          welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
          StackPane.setAlignment(welcomeLabel, Pos.CENTER);
          contentWrapper.getChildren().setAll(welcomeLabel);

          this.selectedGameType = null;
          saveGameButton.setDisable(true);
          loadGameButton.setDisable(true);
        }
    );
    contentWrapper.getChildren().setAll(chooseGameView.getRoot());
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(false);
  }

  /**
   * Placeholder for showing the Player Configuration View. This will be implemented in Phase 3.
   *
   * @param gameType The type of game selected
   */
  private void showPlayerConfigurationView(String gameType) {
    logger.info("Transitioning to Player Configuration for game: {}", gameType);
    Label configPlaceholder = new Label("Player Configuration for " + gameType + "\n\n"
        + "(This will allow setting up players, names, colors, etc.)");
    configPlaceholder.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
    configPlaceholder.setWrapText(true);
    StackPane.setAlignment(configPlaceholder, Pos.CENTER);
    contentWrapper.getChildren().setAll(configPlaceholder);

    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);
  }


  private Button buildSaveButton() {
    Button button = new Button("Save game");
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

          if (currentController == null) {
            logger.warn(
                "No game context for loading. Attempting to set up Snakes and Ladders context.");
            loadSnakesAndLaddersForLoading();
          }

          if (currentController == null) {
            logger.error("Failed to initialize a game controller for loading.");
            LoggingNotification.error("Failed to load game", "No game controller available.");
            return;
          }

          currentController.loadGameState(file.getPath());
          saveGameButton.setDisable(false);
          loadGameButton.setDisable(false); // Still possible to load another game.
        });
    return button;
  }

  private void loadSnakesAndLaddersForLoading() {
    Path savesDir = Path.of("saves");
    try {
      Files.createDirectories(savesDir);
    } catch (IOException e) {
      logger.error("Failed to create saves directory: {}", e.getMessage());
      LoggingNotification.error("Failed to create saves directory", "Cannot load game.");
      this.currentController = null;
      return;
    }

    var repo = new JsonSnlGameStateRepository();
    SnlController controller = new SnlController(2, repo);

    this.currentController = controller;
    SnlView view = new SnlView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());
  }

  @SuppressWarnings("unused")
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
    SnlController controller = new SnlController(2, repo);

    this.currentController = controller;
    SnlView view = new SnlView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());

    saveGameButton.setDisable(false);
    loadGameButton.setDisable(false);
  }

  @SuppressWarnings("unused")
  private void loadCluedo() {
    int numberOfPlayers = 6;
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
