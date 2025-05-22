package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
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
import java.util.List;
import java.util.Optional;
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
          this.currentController = null;
          saveGameButton.setDisable(true);
          loadGameButton.setDisable(true);
        }
    );
    contentWrapper.getChildren().setAll(chooseGameView.getRoot());
    this.currentController = null;
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(false);
  }

  private void showPlayerConfigurationView(String gameType) {
    PlayerConfigurationView configView = new PlayerConfigurationView(
        gameType,
        (type, playerDetailsList) -> {
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
    this.currentController = null;
    saveGameButton.setDisable(true);
    loadGameButton.setDisable(true);
  }

  private void startGame(String gameType, List<PlayerSetupDetails> playerDetailsList) {
    logger.info("Starting game {} with {} players.", gameType, playerDetailsList.size());
    this.currentController = null;

    try {
      if (ChooseGameView.GAME_SNAKES_AND_LADDERS.equals(gameType)) {
        var repo = new JsonSnlGameStateRepository();
        SnlController snlController = new SnlController(playerDetailsList, repo);
        this.currentController = snlController;
        SnlView snlView = new SnlView(snlController);
        contentWrapper.getChildren().setAll(snlView.getRoot());
        saveGameButton.setDisable(false);
        loadGameButton.setDisable(false);
      } else if (ChooseGameView.GAME_CLUEDO.equals(gameType)) {
        CluedoController cluedoController = new CluedoController(playerDetailsList);
        this.currentController = cluedoController;
        CluedoView cluedoView = new CluedoView(cluedoController);
        contentWrapper.getChildren().setAll(cluedoView.getRoot());
        saveGameButton.setDisable(true);
        loadGameButton.setDisable(true);
      } else {
        LoggingNotification.error("Unknown Game Type", "Cannot start game: " + gameType);
        showChooseGameView();
        return;
      }
      LoggingNotification.info("Game Started",
          getGameDisplayName(gameType) + " started with " + playerDetailsList.size()
              + " players.");
    } catch (Exception e) {
      logger.error("Error starting game {}: {}", gameType, e.getMessage(), e);
      LoggingNotification.error("Game Start Failed",
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
          if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
              logger.error("Failed to create saves directory. Cannot save game.");
              LoggingNotification.error("Failed to create saves directory", "Cannot save game.");
              return;
            }
          }
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
    SnlController controller;
    try {
      List<PlayerSetupDetails> dummyDetails = List.of(
          new PlayerSetupDetails("Dummy1", Optional.of(PlayerColor.RED), Optional.empty()),
          new PlayerSetupDetails("Dummy2", Optional.of(PlayerColor.BLUE), Optional.empty())
      );
      controller = new SnlController(dummyDetails, repo);

    } catch (Exception ex) {
      logger.error("Failed to init SnlController for loading: {}", ex.getMessage(), ex);
      this.currentController = null;
      return;
    }

    this.currentController = controller;
    SnlView view = new SnlView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());
  }

  @SuppressWarnings("unused")
  private void loadSnakesAndLadders() {
    // This method is effectively replaced by the new startGame flow.
  }

  @SuppressWarnings("unused")
  private void loadCluedo() {
    // This method is effectively replaced by the new startGame flow.
  }

  private Stage getStage() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      return (Stage) root.getScene().getWindow();
    }
    return null;
  }
}
