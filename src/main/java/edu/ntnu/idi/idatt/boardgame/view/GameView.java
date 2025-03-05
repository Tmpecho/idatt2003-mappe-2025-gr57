package edu.ntnu.idi.idatt.boardgame.view;

import edu.ntnu.idi.idatt.boardgame.controller.common.GameController;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** The GameView class. */
public class GameView {
  private final Button rollDiceButton;
  private final Label logLabel;
  private final BorderPane root;
  private final List<GameController> gameControllers;

  /** Constructor for GameView. */
  public GameView(List<GameController> gameControllers) {
    this.gameControllers = gameControllers;
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    VBox mainLayout = new VBox(10);

    mainLayout
        .getChildren()
        .addAll(gameControllers.get(0).getGameBoard(), rollDiceButton, logLabel);

    root = new BorderPane();
    root.setCenter(mainLayout);

    setupRollDiceButton();
  }

  /** Setup the roll dice button. */
  private void setupRollDiceButton() {
    rollDiceButton.setOnAction(e -> gameControllers.get(0).onRoll());
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
