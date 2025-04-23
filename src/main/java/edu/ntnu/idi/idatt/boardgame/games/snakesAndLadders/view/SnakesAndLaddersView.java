package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view;

import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameObserver;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller.SnakesAndLaddersController;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board.SnakesAndLaddersBoard;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SnakesAndLaddersView implements GameObserver {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  public SnakesAndLaddersView(SnakesAndLaddersController controller) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    SnakesAndLaddersBoard board = (SnakesAndLaddersBoard) controller.getGameBoard();
    SnakesAndLaddersBoardView boardView = new SnakesAndLaddersBoardView(board);

    VBox mainLayout = new VBox(10);
    mainLayout.getChildren().addAll(boardView.getNode(), rollDiceButton, logLabel);
    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton(controller);
    controller.addObserver(this);
  }

  private void setupRollDiceButton(GameController controller) {
    rollDiceButton.setOnAction(
        e -> {
          if (controller instanceof SnakesAndLaddersController) {
            ((SnakesAndLaddersController) controller).rollDice();
          }
        });
  }

  @Override
  public void update(String message) {
    logLabel.setText(message);
  }

  @Override
  public void gameFinished(Player currentPlayer) {
    logLabel.setText(currentPlayer.getName() + " wins!");
    rollDiceButton.setDisable(true);
  }

  public BorderPane getRoot() {
    return root;
  }
}
