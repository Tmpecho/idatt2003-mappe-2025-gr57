package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class CluedoView implements GameObserver<GridPos> {
    private final BorderPane root;
    private final CluedoBoardView boardView;
    private final Label statusLabel;
    private final Button rollDiceButton; // Example action button
    private final CluedoController controller;

    public CluedoView(CluedoController controller) {
        this.controller = controller;
        this.root = new BorderPane();
        root.setPadding(new Insets(10));

        CluedoBoard board = (CluedoBoard) controller.getGameBoard();
        this.boardView = new CluedoBoardView(board);
        ScrollPane scrollPane = new ScrollPane(boardView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);


        // Control Panel (Right Side)
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f0f0f0;");

        this.statusLabel = new Label("Welcome to Cluedo!");
        statusLabel.setWrapText(true);

        this.rollDiceButton = new Button("Roll Dice & Move");
        rollDiceButton.setOnAction(e -> controller.rollDiceAndMove());

        // TODO: Add more controls
        Button suggestButton = new Button("Make Suggestion");
        suggestButton.setOnAction(e -> controller.makeSuggestion());
        suggestButton.setDisable(true); // Enable only when in a room

        Button accuseButton = new Button("Make Accusation");
        accuseButton.setOnAction(e -> controller.makeAccusation());

    controlPanel.getChildren().addAll(statusLabel, rollDiceButton, suggestButton, accuseButton);

    root.setRight(controlPanel);

        controller.addObserver(this);
        update("Game started. It's " + controller.getCurrentPlayer().getName() + "'s turn.");
    }

    public BorderPane getRoot() {
        return root;
    }

    @Override
    public void update(String message) {
        statusLabel.setText(message);
    boardView.highlightTile(controller.getCurrentPlayer().getPosition());
    }

    @Override
    public void gameFinished(Player<GridPos> winner) {
        statusLabel.setText("Game Over! " + winner.getName() + " wins!");
        // Disable game action buttons
        rollDiceButton.setDisable(true);
        // suggestButton.setDisable(true);
        // accuseButton.setDisable(true);
    }
}
