package edu.ntnu.idi.idatt.boardgame.common.view;

import java.io.File;

import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view.SnakesAndLaddersView;
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
                // Ensure the 'saves' directory exists or handle potential errors
                File savesDir = new File("saves");
                if (!savesDir.exists()) {
                    savesDir.mkdirs(); // Create directory if it doesn't exist
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
        loadGameButton.setDisable(true); // Initially disabled, enabled when a game is loaded
        loadGameButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Game State");
            // Ensure the 'saves' directory exists or handle potential errors
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                System.out.println("Saves directory does not exist. Cannot load game.");
                return; // Or handle differently, e.g., allow navigating elsewhere
            }
            fileChooser.setInitialDirectory(savesDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            Stage stage = getStage();
            if (stage != null) {
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    // Determine which game type to load based on file or context if needed
                    // For now, assuming loading into the current game type context
                    if (currentController == null) {
                        // If no game is active, potentially load Snakes and Ladders as default or based
                        // on file
                        // This part might need more sophisticated logic depending on requirements
                        System.out.println("Loading default game (Snakes and Ladders) for the save file.");
                        loadSnakesAndLadders(); // Load the game structure first
                    }

                    if (currentController != null) {
                        currentController.loadGameState(file.getPath());
                        // After loading, the view might need to be refreshed to reflect the loaded
                        // state.
                        // This depends on how loadGameState and the view interact.
                        // If loadGameState updates the model and the view observes it, it might update
                        // automatically.
                        // Otherwise, manual refresh logic is needed here.
                        System.out.println("Game state loaded. View refresh might be needed.");
                        // Example: if view needs explicit refresh:
                        // if (currentController instanceof SnakesAndLaddersController &&
                        // contentWrapper.getChildren().get(0) instanceof SnakesAndLaddersView) {
                        // SnakesAndLaddersView currentView = (SnakesAndLaddersView)
                        // contentWrapper.getChildren().get(0);
                        // currentView.refreshView(); // Assuming such a method exists
                        // }
                        saveGameButton.setDisable(false); // Ensure save is enabled after load
                        loadGameButton.setDisable(false); // Ensure load stays enabled
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
        // Store the controller instance
        this.currentController = controller;
        SnakesAndLaddersView view = new SnakesAndLaddersView(controller, controller.getGameBoard());
        contentWrapper.getChildren().setAll(view.getRoot());
        // Enable the save and load buttons once a game is loaded
        saveGameButton.setDisable(false);
        loadGameButton.setDisable(false);
    }

    /**
     * Helper method to get the Stage from the root pane.
     * 
     * @return The Stage, or null if the scene or window is not available.
     */
    private Stage getStage() {
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            return (Stage) root.getScene().getWindow();
        }
        return null;
    }
}