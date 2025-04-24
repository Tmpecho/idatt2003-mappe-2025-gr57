package edu.ntnu.idi.idatt.boardgame.ui;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.JsonSnLGameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view.SnLView;
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

/**
 * The {@code MainView} class serves as the primary container for the application, featuring a
 * sidebar with game selection and placeholders for save/load functionality.
 */
public class MainView {
  private final BorderPane root;
  private final StackPane contentWrapper;
  private GameController currentController;
  private Button saveGameButton;
  private Button loadGameButton;

  public MainView() {
    root = new BorderPane();

    contentWrapper = new StackPane();
    contentWrapper.setPadding(new Insets(10));
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

    Label titleLabel = new Label("Board games");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

    VBox gamesBox = new VBox(10);
    addGames(gamesBox);

    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    saveGameButton = buildSaveButton();
    loadGameButton = buildLoadButton();

    Button exitButton = new Button("Exit");
    exitButton.setOnAction(e -> Platform.exit());

    sidebar
        .getChildren()
        .addAll(titleLabel, gamesBox, spacer, saveGameButton, loadGameButton, exitButton);
    root.setLeft(sidebar);
  }

  private Button buildSaveButton() {
    Button button = new Button("Save game");
    button.setDisable(true);
    button.setOnAction(
        e -> {
          if (currentController == null) {
            System.out.println("No game loaded to save.");
            return;
          }
          FileChooser chooser = new FileChooser();
          chooser.setTitle("Save Game State");
          File dir = new File("saves");
          if (!dir.exists()) {
            dir.mkdirs();
          }
          chooser.setInitialDirectory(dir);
          chooser.setInitialFileName("game_state.json");
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
          Stage stage = getStage();
          if (stage == null) {
            System.err.println("Could not get the stage to show save dialog.");
            return;
          }
          File file = chooser.showSaveDialog(stage);
          if (file == null) {
            System.out.println("Save cancelled.");
            return;
          }
          currentController.saveGameState(file.getPath());
        });
    return button;
  }

  private Button buildLoadButton() {
    Button button = new Button("Load game");
    button.setDisable(true);
    button.setOnAction(
        e -> {
          FileChooser chooser = new FileChooser();
          chooser.setTitle("Load Game State");
          File dir = new File("saves");
          if (!dir.exists()) {
            System.out.println("Saves directory does not exist. Cannot load game.");
            return;
          }
          chooser.setInitialDirectory(dir);
          chooser
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
          Stage stage = getStage();
          if (stage == null) {
            System.err.println("Could not get the stage to show load dialog.");
            return;
          }
          File file = chooser.showOpenDialog(stage);
          if (file == null) {
            System.out.println("Load cancelled.");
            return;
          }
          if (currentController == null) {
            System.out.println("Loading default game (Snakes and Ladders) for the save file.");
            loadSnakesAndLadders();
          }
          if (currentController == null) {
            System.err.println("Failed to initialize a game controller for loading.");
            return;
          }
          currentController.loadGameState(file.getPath());
          saveGameButton.setDisable(false);
          loadGameButton.setDisable(false);
        });
    return button;
  }

  private void addGames(VBox games) {
    Button snakesAndLaddersButton = new Button("Snakes and Ladders");
    snakesAndLaddersButton.setOnAction(e -> loadSnakesAndLadders());

    Button cluedoButton = new Button("Cluedo");
    cluedoButton.setOnAction(e -> System.out.println("Cluedo not implemented yet."));

    games.getChildren().addAll(snakesAndLaddersButton, cluedoButton);
  }

  private void loadSnakesAndLadders() {
    Path savesDir = Path.of("saves");
    try {
      Files.createDirectories(savesDir);
    } catch (IOException e) {
      System.err.println("Failed to create saves directory: " + e.getMessage());
      return;
    }

    var repo = new JsonSnLGameStateRepository();
    SnLController controller = new SnLController(2, repo);

    this.currentController = controller;
    SnLView view = new SnLView(controller);
    contentWrapper.getChildren().setAll(view.getRoot());

    saveGameButton.setDisable(false);
    loadGameButton.setDisable(false);
  }

  private Stage getStage() {
    if (root.getScene() != null && root.getScene().getWindow() != null) {
      return (Stage) root.getScene().getWindow();
    }
    return null;
  }
}
