package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view;

import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameObserver;
import edu.ntnu.idi.idatt.boardgame.common.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller.SnakesAndLaddersController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SnakesAndLaddersView implements GameObserver {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  public SnakesAndLaddersView(GameController controller, GameBoard gameBoard) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");
    VBox mainLayout = new VBox(10);
    mainLayout.getChildren().addAll(gameBoard.getNode(), rollDiceButton, logLabel);
    root = new BorderPane();
    root.setCenter(mainLayout);
    setupRollDiceButton(controller);
    controller.addObserver(this);
  }

  private void setupRollDiceButton(GameController controller) {
    rollDiceButton.setOnAction(e -> {
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