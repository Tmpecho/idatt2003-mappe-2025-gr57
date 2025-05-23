package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.JsonCluedoGameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.view.CluedoView;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnlController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.JsonSnlGameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view.SnlView;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import edu.ntnu.idi.idatt.boardgame.ui.view.ChooseGameView;
import edu.ntnu.idi.idatt.boardgame.ui.view.PlayerConfigurationView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

  /**
   * Constructs the main view of the application. Initializes the root layout and loads the main
   * menu sidebar.
   */
  public MainView() {
    root = new BorderPane();

    contentWrapper = new StackPane();
    contentWrapper.setPadding(new Insets(10));
    Label welcomeLabel = new Label("Welcome! Click 'Menu' to start.");
    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
    StackPane.setAlignment(welcomeLabel, Pos.CENTER);
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

    Path savesDir = Path.of("saves");
    try {
      Files.createDirectories(savesDir);
    } catch (IOException e) {
      logger.error("Failed to create saves directory: {}", e.getMessage(), e);
      LoggingNotification.error("Failed to create saves directory", "Cannot load game.");
      // Application might still run, but saving/loading will likely fail.
    }

    saveGameButton = buildSaveButton();
    loadGameButton = buildLoadButton();
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);

    Button exitButton = new Button("Exit");
    exitButton.setOnAction(e -> Platform.exit());
    exitButton.setMaxWidth(Double.MAX_VALUE);
    saveGameButton.setMaxWidth(Double.MAX_VALUE);
    loadGameButton.setMaxWidth(Double.MAX_VALUE);

    sidebar.getChildren().addAll(menuButton, spacer, saveGameButton, loadGameButton, exitButton);
    root.setLeft(sidebar);
  }

  private void showChooseGameView() {
    ChooseGameView chooseGameView =
        new ChooseGameView(
            gameType -> {
              logger.info("Game selected: {}", gameType);
              showPlayerConfigurationView(gameType);
              loadGameButton.setDisable(false); // Enable load once a game type context is selected
            },
            () -> {
              logger.info("Back to main menu from ChooseGameView.");
              Label welcomeLabel = new Label("Welcome! Click 'Menu' to start.");
              welcomeLabel.setStyle("-fx-font-size: 16px; -fx-alignment: center;");
              StackPane.setAlignment(welcomeLabel, Pos.CENTER);
              contentWrapper.getChildren().setAll(welcomeLabel);
              this.currentController = null;
              saveGameButton.setDisable(true);
              loadGameButton.setDisable(true);
            });
    contentWrapper.getChildren().setAll(chooseGameView.getRoot());
    this.currentController = null;
    saveGameButton.setDisable(true);
  }

  private void showPlayerConfigurationView(String gameType) {
    PlayerConfigurationView configView =
        new PlayerConfigurationView(
            gameType,
            (type, playerDetailsList) -> {
              logger.info(
                  "Attempting to start game: {} with {} players.", type, playerDetailsList.size());
              playerDetailsList.forEach(pd -> logger.debug("Player Detail: {}", pd));
              startGame(type, playerDetailsList);
            },
            () -> {
              logger.info("Back to game selection from PlayerConfigurationView.");
              showChooseGameView();
            });
    contentWrapper.getChildren().setAll(configView.getRoot());
    this.currentController = null;
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);
  }

  private void startGame(String gameType, List<PlayerSetupDetails> playerDetailsList) {
    logger.info("Starting game {} with {} players.", gameType, playerDetailsList.size());
    this.currentController = null;

    try {
      if (ChooseGameView.GAME_SNAKES_AND_LADDERS.equals(gameType)) {
        JsonSnlGameStateRepository repo = new JsonSnlGameStateRepository();
        SnlController snlController = new SnlController(playerDetailsList, repo);
        this.currentController = snlController;
        SnlView snlView = new SnlView(snlController);
        contentWrapper.getChildren().setAll(snlView.getRoot());
        saveGameButton.setDisable(false);
        loadGameButton.setDisable(false);
      } else if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        JsonCluedoGameStateRepository cluedoRepo = new JsonCluedoGameStateRepository();
        CluedoController cluedoController = new CluedoController(playerDetailsList, cluedoRepo);
        this.currentController = cluedoController;
        CluedoView cluedoView = new CluedoView(cluedoController);
        contentWrapper.getChildren().setAll(cluedoView.getRoot());
        saveGameButton.setDisable(false);
        loadGameButton.setDisable(false);
      } else {
        LoggingNotification.error("Unknown Game Type", "Cannot start game: " + gameType);
        showChooseGameView();
        return;
      }
      LoggingNotification.info(
          "Game Started",
          getGameDisplayName(gameType) + " started with " + playerDetailsList.size() + " players.");
    } catch (Exception e) {
      logger.error("Error starting game {}: {}", gameType, e.getMessage(), e);
      LoggingNotification.error(
          "Game Start Failed",
          "Could not start " + getGameDisplayName(gameType) + ": " + e.getMessage());
      showChooseGameView();
    }
  }

  private String getGameDisplayName(String type) {
    if (ChooseGameView.GAME_SNAKES_AND_LADDERS.equals(type)) {
      return "Snakes and Ladders";
    } else if (ChooseGameView.GAME_CLUEDO.equals(type)) {
      return "Cluedo";
    }
    return "Unknown Game";
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
          chooser.setInitialDirectory(dir);
          String initialFileName = "game_state.json";
          if (currentController instanceof SnlController) {
            initialFileName = "snl_save.json";
          } else if (currentController instanceof CluedoController) {
            initialFileName = "cluedo_save.json";
          }
          chooser.setInitialFileName(initialFileName);
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
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
          chooser.setInitialDirectory(dir);
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
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

          String filePath = file.getPath();
          String fileName = file.getName().toLowerCase();
          boolean loadedSuccessfully = false;

          // Empty list for PlayerSetupDetails when creating controller for loading
          List<PlayerSetupDetails> emptyPlayerDetails = new ArrayList<>();

          if (fileName.contains("cluedo")) {
            logger.info("Attempting to load as Cluedo game: {}", filePath);
            try {
              JsonCluedoGameStateRepository cluedoRepo = new JsonCluedoGameStateRepository();
              CluedoController cluedoController =
                  new CluedoController(emptyPlayerDetails, cluedoRepo);
              this.currentController = cluedoController; // Set controller BEFORE load
              cluedoController.loadGameState(
                  filePath); // Now load can populate players in the controller
              CluedoView cluedoView =
                  new CluedoView(cluedoController); // View after controller is ready
              contentWrapper.getChildren().setAll(cluedoView.getRoot());
              saveGameButton.setDisable(false);
              loadGameButton.setDisable(false); // Re-enable load, game is active
              loadedSuccessfully = true;
            } catch (Exception ex) {
              logger.error("Failed to load Cluedo game from {}: {}", filePath, ex.getMessage(), ex);
              LoggingNotification.error(
                  "Load Failed", "Could not load Cluedo game. " + ex.getMessage());
              this.currentController = null;
            }
          } else if (fileName.contains("snl")) {
            logger.info("Attempting to load as Snakes and Ladders game: {}", filePath);
            try {
              JsonSnlGameStateRepository snlRepo = new JsonSnlGameStateRepository();
              SnlController snlController = new SnlController(emptyPlayerDetails, snlRepo);
              this.currentController = snlController;
              snlController.loadGameState(filePath);
              SnlView snlView = new SnlView(snlController);
              contentWrapper.getChildren().setAll(snlView.getRoot());
              saveGameButton.setDisable(false);
              loadGameButton.setDisable(false);
              loadedSuccessfully = true;
            } catch (Exception ex) {
              logger.error(
                  "Failed to load Snakes and Ladders game from {}: {}",
                  filePath,
                  ex.getMessage(),
                  ex);
              LoggingNotification.error(
                  "Load Failed", "Could not load S&L game. " + ex.getMessage());
              this.currentController = null;
            }
          } else {
            LoggingNotification.warn(
                "Load Canceled",
                "Could not determine game type from filename. "
                    + "Please ensure filename contains 'cluedo' or 'snl'.");
          }

          if (!loadedSuccessfully && this.currentController == null) {
            showChooseGameView();
            saveGameButton.setDisable(true);
          }
        });
    return button;
  }

  private Stage getStage() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      return (Stage) root.getScene().getWindow();
    }
    return null;
  }
}
