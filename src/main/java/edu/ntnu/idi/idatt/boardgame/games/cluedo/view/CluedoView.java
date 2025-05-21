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

/**
 * The main view for the Cluedo game. It displays the game board and control panel.
 * Implements {@link GameObserver} to react to game state changes.
 */
public final class CluedoView implements GameObserver<GridPos> {
  private final BorderPane root;
  private final CluedoBoardView boardView;
  private final Label statusLabel;
  private final Button rollDiceButton;
  private final CluedoController controller;

  /**
   * Constructs the Cluedo game view.
   *
   * @param controller The {@link CluedoController} managing the game logic.
   */
  public CluedoView(CluedoController controller) {
    this.controller = controller;
    this.root = new BorderPane();
    root.setPadding(new Insets(10));

    CluedoBoard board = (CluedoBoard) controller.getGameBoard();
    this.boardView =
            new CluedoBoardView(
                    board, () -> controller.getCurrentPlayer().getPosition(), controller::movePlayerTo);

    ScrollPane scrollPane = new ScrollPane(boardView);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    root.setCenter(scrollPane);

    // Control Panel (Right Side)
    VBox controlPanel = new VBox(10);
    controlPanel.setPrefWidth(200);
    controlPanel.setPadding(new Insets(10));
    controlPanel.setStyle("-fx-background-color: #f0f0f0;");

    this.statusLabel = new Label("Welcome to Cluedo!");
    statusLabel.setWrapText(true);

    this.rollDiceButton = new Button("Roll Dice & Move");
    rollDiceButton.setOnAction(
            e -> {
              controller.rollDiceAndMove();
              rollDiceButton.setDisable(true);
            });

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

  /**
   * Returns the root {@link BorderPane} of this view.
   *
   * @return The root pane.
   */
  public BorderPane getRoot() {
    return root;
  }

  @Override
  public void update(String message) {
    statusLabel.setText(message);
    boardView.highlightTile(controller.getCurrentPlayer().getPosition());

    boolean waitingForRoll = controller.getStepsLeft() == 0;
    rollDiceButton.setDisable(!waitingForRoll);
  }

  @Override
  public void gameFinished(Player<GridPos> winner) {
    statusLabel.setText("Game Over! " + winner.getName() + " wins!");
    // Disable game action buttons
    rollDiceButton.setDisable(true);
    // TODO: Consider diabling these too
    // suggestButton.setDisable(true);
    // accuseButton.setDisable(true);
  }
}
