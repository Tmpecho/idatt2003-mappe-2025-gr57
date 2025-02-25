package edu.ntnu.idi.idatt.boardgame.controller;

import edu.ntnu.idi.idatt.boardgame.action.CompositeAction;
import edu.ntnu.idi.idatt.boardgame.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.player.Player;
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

  private final GameLoop gameLoop;

  public GameController() {
    this.dice = new Dice(2);
    this.players = createPlayers();
    this.gameBoard = new GameBoard();
    this.rollDiceButton = new Button("Roll dice");
    this.logLabel = new Label("Game log:");

    gameBoard.addPlayersToStart(players);
    this.currentPlayer = players.get(1);

    gameLoop = new GameLoop();
    setupRollDiceButton();
  }

  private void setupRollDiceButton() {
    rollDiceButton.setOnAction(e -> gameLoop.onRoll());
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

  private class GameLoop {
    private CompositeAction currentAction = new CompositeAction();

    public void onRoll() {
      currentAction.addAction(new RollAction(gameBoard, currentPlayer, dice));

      String actionMessage = currentAction.execute();
      logLabel.setText(actionMessage);

      if (playerWon()) {
        onGameFinish();
      } else {
        currentPlayer = getNextPlayer();
        currentAction = new CompositeAction();
      }
    }

    private boolean playerWon() {
      return currentPlayer.getPosition() == GameBoard.getBoardSize();
    }

    private void onGameFinish() {
      logLabel.setText("Player " + currentPlayer.getId() + " wins!");
      rollDiceButton.setDisable(true);
    }
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
