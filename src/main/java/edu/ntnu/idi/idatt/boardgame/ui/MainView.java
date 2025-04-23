package edu.ntnu.idi.idatt.boardgame.ui;

import java.io.File;

import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view.SnakesAndLaddersView;
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
 * The {@code MainView} class serves as the primary container for the
 * application, featuring a
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

        VBox games = new VBox(10);
        addGames(games);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        saveGameButton = new Button("Save game");
        saveGameButton.setDisable(true);
        saveGameButton.setOnAction(e -> {
            if (currentController != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Game State");
                File savesDir = new File("saves");
                if (!savesDir.exists()) {
                    savesDir.mkdirs();
                }
                fileChooser.setInitialDirectory(savesDir);
                fileChooser.setInitialFileName("game_state.json");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                Stage stage = getStage();
                if (stage != null) {
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        currentController.saveGameState(file.getPath());
                    } else {
                        System.out.println("Save cancelled.");
                    }
                } else {
                    System.err.println("Could not get the stage to show save dialog.");
                }
            } else {
                System.out.println("No game loaded to save.");
            }
        });

        loadGameButton = new Button("Load game");
        loadGameButton.setDisable(true);
        loadGameButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Game State");
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                System.out.println("Saves directory does not exist. Cannot load game.");
                return;
            }
            fileChooser.setInitialDirectory(savesDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            Stage stage = getStage();
            if (stage != null) {
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    if (currentController == null) {
                        System.out.println("Loading default game (Snakes and Ladders) for the save file.");
                        loadSnakesAndLadders();
                    }

                    if (currentController != null) {
                        currentController.loadGameState(file.getPath());
                        System.out.println("Game state loaded. View refresh might be needed.");
                        saveGameButton.setDisable(false);
                        loadGameButton.setDisable(false);
                    } else {
                        System.err.println("Failed to initialize a game controller for loading.");
                    }
                } else {
                    System.out.println("Load cancelled.");
                }
            } else {
                System.err.println("Could not get the stage to show load dialog.");
            }
        });

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        sidebar.getChildren().addAll(titleLabel, games, spacer, saveGameButton, loadGameButton, exitButton);
        root.setLeft(sidebar);
    }

    private void addGames(VBox games) {
        Button snakesAndLaddersButton = new Button("Snakes and Ladders");
        snakesAndLaddersButton.setOnAction(e -> loadSnakesAndLadders());

        Button cluedoButton = new Button("Cluedo");
        cluedoButton.setOnAction(e -> System.out.println("Cluedo not implemented yet."));

        games.getChildren().addAll(snakesAndLaddersButton, cluedoButton);
    }

    private void loadSnakesAndLadders() {
        SnakesAndLaddersController controller = new SnakesAndLaddersController(2);
        this.currentController = controller;
        SnakesAndLaddersView view = new SnakesAndLaddersView(controller);
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