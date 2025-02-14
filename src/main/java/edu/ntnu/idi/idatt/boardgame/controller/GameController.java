package edu.ntnu.idi.idatt.boardgame.controller;

import edu.ntnu.idi.idatt.boardgame.model.Dice;
import edu.ntnu.idi.idatt.boardgame.model.GameBoard;
import edu.ntnu.idi.idatt.boardgame.model.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class GameController {
  private final GameBoard gameBoard;
  private final Dice dice;
  private final Button rollDiceButton;
  private final Label logLabel;

  private final int numberOfPlayers = 2;
  private final List<Color> playerColors =
      List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);
  private final Map<Integer, Player> players;
  private Player currentPlayer;
  private boolean gameOver = false;

  public GameController() {
    this.dice = new Dice(2);
    this.players = createPlayers();
    this.gameBoard = new GameBoard();
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    gameBoard.addPlayersToStart(players);
    this.currentPlayer = players.get(1);

    setupRollDiceButton();
  }

  /**
   * Sets up the dice roll button event. After rolling, the move (and any connector effect) is
   * applied and the log label is updated.
   */
  private void setupRollDiceButton() {
    rollDiceButton.setOnAction(
        e -> {
          if (gameOver) {
            return;
          }

          int roll = dice.roll();
          String logMessage = gameBoard.incrementPlayerPosition(currentPlayer, roll);
          logLabel.setText(logMessage);

          if (currentPlayer.getPosition() != GameBoard.getBoardSize()) {
            currentPlayer = getNextPlayer();
          } else {
            gameOver = true;
            logLabel.setText("Player " + currentPlayer.getId() + " wins!");
            rollDiceButton.setDisable(true);
          }
        });
  }

  private Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  private Map<Integer, Player> createPlayers() {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              Player player = new Player(playerId, playerColors.get(playerId - 1));
              players.put(playerId, player);
            });
    return players;
  }

  public GameBoard getGameBoard() {
    return gameBoard;
  }

  public Button getRollDiceButton() {
    return rollDiceButton;
  }

  public Label getLogLabel() {
    return logLabel;
  }
}
