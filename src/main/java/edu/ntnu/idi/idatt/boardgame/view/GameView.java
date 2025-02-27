package edu.ntnu.idi.idatt.boardgame.view;

import edu.ntnu.idi.idatt.boardgame.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class GameView {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  public GameView(GameController controller, GameBoard gameBoard) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    VBox mainLayout = new VBox(10);

    mainLayout.getChildren().addAll(
            gameBoard,
            rollDiceButton,
            logLabel
    );

    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton(controller);
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

  public void updateLogText(String text) {
    logLabel.setText(text);
  }

  public void disableRollButton() {
    rollDiceButton.setDisable(true);
  }

  public BorderPane getRoot() {
    return root;
  }
}