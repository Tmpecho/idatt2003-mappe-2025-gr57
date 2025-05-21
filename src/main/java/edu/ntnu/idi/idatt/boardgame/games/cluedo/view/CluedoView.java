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
  private final Button suggestButton;
  private final Button accuseButton;
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
    boardView =
        new CluedoBoardView(
            board, () -> controller.getCurrentPlayer().getPosition(), controller::onBoardClick);
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

    this.rollDiceButton = new Button("Roll Dice");
    rollDiceButton.setOnAction(
        e -> {
          controller.onRollButton();
          rollDiceButton.setDisable(true);
        });

    this.suggestButton = new Button("Make Suggestion");
    suggestButton.setOnAction(e -> controller.makeSuggestion());
    suggestButton.setDisable(true); // Enable only when in a room excluding the "Cluedo" room

    this.accuseButton = new Button("Make Accusation");
    accuseButton.setOnAction(e -> controller.makeAccusation());
    accuseButton.setDisable(true); // Enable only when in "Cluedo room"

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

    boolean waitingForRoll = controller.isWaitingForRoll();
    rollDiceButton.setDisable(!waitingForRoll);

    suggestButton.setDisable(!controller.canSuggest());
    accuseButton.setDisable(!controller.canAccuse());
  }

  @Override
  public void gameFinished(Player<GridPos> winner) {
    statusLabel.setText("Game Over! " + winner.getName() + " wins!");
    // Disable game action buttons
    rollDiceButton.setDisable(true);
    suggestButton.setDisable(true);
    accuseButton.setDisable(true);
  }
}
