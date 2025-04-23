package edu.ntnu.idi.idatt.boardgame.common.view;

import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view.SnakesAndLaddersView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

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
                currentController.saveGameState("saves/game_state.json");
            } else {
                System.out.println("No game loaded to save.");
            }
        });

        loadGameButton = new Button("Load game");
        loadGameButton.setDisable(true);
        loadGameButton.setOnAction(e -> {
            if (currentController != null) {
                currentController.loadGameState("saves/game_state.json");
            } else {
                System.out.println("No game loaded to load.");
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
}