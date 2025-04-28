package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnLBoard;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SnLView implements GameObserver<LinearPos> {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  public SnLView(SnLController controller) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    SnLBoard board = (SnLBoard) controller.getGameBoard();
    SnLBoardView boardView = new SnLBoardView(board);

    VBox mainLayout = new VBox(10);
    mainLayout.getChildren().addAll(boardView, rollDiceButton, logLabel);
    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton(controller);
    controller.addObserver(this);
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

    if (rollDiceButton.isDisable()) {
      rollDiceButton.setDisable(false);
    }
  }

  @Override
  public void gameFinished(Player<LinearPos> currentPlayer) {
    logLabel.setText(currentPlayer.getName() + " wins!");
    rollDiceButton.setDisable(true);
  }

  public BorderPane getRoot() {
    return root;
  }
}
