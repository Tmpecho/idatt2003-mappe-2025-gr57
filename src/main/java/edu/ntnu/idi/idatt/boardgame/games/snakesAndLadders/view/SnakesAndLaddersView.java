package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.view;

import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameObserver; // Added import
import edu.ntnu.idi.idatt.boardgame.common.domain.board.GameBoard;
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
    rollDiceButton.setOnAction(e -> controller.onRoll());
  }

  public Button getRollDiceButton() {
    return rollDiceButton;
  }

  public Label getLogLabel() {
    return logLabel;
  }

  public void updateLogText(String text) { // Keep for compatibility, but not used directly
    logLabel.setText(text);
  }

  public void disableRollButton() { // Keep for compatibility, but not used directly
    rollDiceButton.setDisable(true);
  }

  @Override
  public void update(String message) { // Implement observer method
    logLabel.setText(message);
  }

  @Override
  public void gameFinished(int winnerId) {
    logLabel.setText("Player " + winnerId + " wins!");
    rollDiceButton.setDisable(true);
  }

  public BorderPane getRoot() {
    return root;
  }
}