package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
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
  private String selectedGameType;

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

  private void showChooseGameView() {
    ChooseGameView chooseGameView = new ChooseGameView(
        gameType -> {
          this.selectedGameType = gameType;
          logger.info("Game selected: {}", gameType);
          showPlayerConfigurationView(gameType);
        },
        () -> {
          logger.info("Back to main menu from ChooseGameView.");
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

  private void showPlayerConfigurationView(String gameType) {
    PlayerConfigurationView configView = new PlayerConfigurationView(
        gameType,
        (type, playerDetailsList) -> { // onStartGame lambda
          logger.info("Attempting to start game: {} with {} players.", type,
              playerDetailsList.size());
          playerDetailsList.forEach(pd -> logger.debug("Player Detail: {}", pd));
          startGame(type, playerDetailsList);
        },
        () -> {
          logger.info("Back to game selection from PlayerConfigurationView.");
          showChooseGameView();
        }
    );
    contentWrapper.getChildren().setAll(configView.getRoot());
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);
  }

  /**
   * Placeholder for the actual game starting logic. This will be fully implemented in Phase 4.
   *
   * @param gameType          The type of game to start (e.g.,
   *                          ChooseGameView.GAME_SNAKES_AND_LADDERS)
   * @param playerDetailsList List of player configurations
   */
  private void startGame(String gameType, List<PlayerSetupDetails> playerDetailsList) {
    logger.info("Starting game {} with {} players.", gameType, playerDetailsList.size());

    if (ChooseGameView.GAME_SNAKES_AND_LADDERS.equals(gameType)) {
      Label gamePlaceholder = new Label("Placeholder for Snakes and Ladders game started with "
          + playerDetailsList.size() + " players.");
      gamePlaceholder.setStyle("-fx-font-size: 16px;");
      contentWrapper.getChildren().setAll(gamePlaceholder);
      LoggingNotification.info("Game Start Placeholder", "Snakes and Ladders would start here.");
      saveGameButton.setDisable(false); // SnL can be saved
      loadGameButton.setDisable(false);
    } else if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
      Label gamePlaceholder = new Label("Placeholder for Cluedo game started with "
          + playerDetailsList.size() + " players.");
      gamePlaceholder.setStyle("-fx-font-size: 16px;");
      contentWrapper.getChildren().setAll(gamePlaceholder);
      LoggingNotification.info("Game Start Placeholder", "Cluedo would start here.");
      saveGameButton.setDisable(true);
      loadGameButton.setDisable(true);
    } else {
      LoggingNotification.error("Unknown Game Type", "Cannot start game: " + gameType);
      showChooseGameView();
      return;
    }
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
          if (currentController instanceof SnlController) {
            saveGameButton.setDisable(false);
          } else if (currentController instanceof CluedoController) {
            saveGameButton.setDisable(true);
          } else {
            saveGameButton.setDisable(true);
          }
          loadGameButton.setDisable(false);
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
  }

  @SuppressWarnings("unused")
  private void loadCluedo() {
  }

  private Stage getStage() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      return (Stage) root.getScene().getWindow();
    }
    return null;
  }
}
