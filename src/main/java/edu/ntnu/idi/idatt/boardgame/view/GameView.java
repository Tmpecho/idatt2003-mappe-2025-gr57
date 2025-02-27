package edu.ntnu.idi.idatt.boardgame.view;

import edu.ntnu.idi.idatt.boardgame.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** The GameView class. */
public class GameView {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;

  /**
   * Constructor for GameView.
   *
   * @param controller the game controller
   * @param gameBoard the game board
   */
  public GameView(GameController controller, GameBoard gameBoard) {
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    VBox mainLayout = new VBox(10);

    mainLayout.getChildren().addAll(gameBoard, rollDiceButton, logLabel);

    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton(controller);
  }

  /**
   * Setup the roll dice button.
   *
   * @param controller the game controller
   */
  private void setupRollDiceButton(GameController controller) {
    rollDiceButton.setOnAction(e -> controller.onRoll());
  }

  /**
   * Get the roll dice button.
   *
   * @return the roll dice button
   */
  public Button getRollDiceButton() {
    return rollDiceButton;
  }

  /**
   * Get the log label.
   *
   * @return the log label
   */
  public Label getLogLabel() {
    return logLabel;
  }

  /**
   * Update the log text.
   *
   * @param text the text
   */
  public void updateLogText(String text) {
    logLabel.setText(text);
  }

  /** Disable the roll button. */
  public void disableRollButton() {
    rollDiceButton.setDisable(true);
  }

  /**
   * Return root pane.
   *
   * @return the root pane
   */
  public BorderPane getRoot() {
    return root;
  }
}
