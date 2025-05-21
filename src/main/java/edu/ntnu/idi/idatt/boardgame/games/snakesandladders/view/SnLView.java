package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnLBoard;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * The main view for the Snakes and Ladders game.
 * It displays the game board, a roll dice button, and a log label.
 * Implements {@link GameObserver} to react to game state changes.
 */
public final class SnLView implements GameObserver<LinearPos> {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  /**
   * Constructs the Snakes and Ladders game view.
   *
   * @param controller The {@link SnLController} managing the game logic.
   */
  public SnLView(SnLController controller) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");
    logLabel.setWrapText(true);

    SnLBoard board = (SnLBoard) controller.getGameBoard();
    SnLBoardView boardView = new SnLBoardView(board);

    VBox mainLayout = new VBox(10);
    mainLayout.setPadding(new Insets(10));
    mainLayout.getChildren().addAll(boardView, rollDiceButton, logLabel);
    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton(controller);
    controller.addObserver(this);
    update("Game started. It's " + controller.getCurrentPlayer().getName() + "'s turn.");
  }

  private void setupRollDiceButton(GameController<LinearPos> controller) {
    rollDiceButton.setOnAction(
            e -> {
              if (controller instanceof SnLController) {
                ((SnLController) controller).rollDice();
              }
            });
  }

  @Override
  public void update(String message) {
    logLabel.setText(message);

    if (rollDiceButton.isDisable() && !message.contains("wins!")) { // A bit heuristic
      rollDiceButton.setDisable(false);
    }
  }

  @Override
  public void gameFinished(Player<LinearPos> currentPlayer) {
    logLabel.setText(currentPlayer.getName() + " wins!");
    rollDiceButton.setDisable(true);
  }

  /**
   * Returns the root {@link BorderPane} of this view.
   *
   * @return The root pane.
   */
  public BorderPane getRoot() {
    return root;
  }
}
