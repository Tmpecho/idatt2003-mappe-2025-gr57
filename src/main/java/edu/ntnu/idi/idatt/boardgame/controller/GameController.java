package edu.ntnu.idi.idatt.boardgame.controller;

import edu.ntnu.idi.idatt.boardgame.action.CompositeAction;
import edu.ntnu.idi.idatt.boardgame.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.view.GameView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.paint.Color;

/**
 * The GameController class.
 */
public class GameController {
  private final GameBoard gameBoard;
  private final Dice dice;
  private final int numberOfPlayers = 2;
  private final List<Color> playerColors =
          List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);
  private final Map<Integer, Player> players;
  private Player currentPlayer;
  private GameView gameView;

  /**
   * Constructor for GameController.
   */
  public GameController() {
    this.dice = new Dice(2);
    this.players = createPlayers();
    this.gameBoard = new GameBoard();

    gameBoard.addPlayersToStart(players);
    this.currentPlayer = players.get(1);
  }

  /**
   * Set the game view.
   *
   * @param gameView the game view
   */
  public void setGameView(GameView gameView) {
    this.gameView = gameView;
  }

  /**
   * Get the current player.
   *
   * @return the current player
   */
  private Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  /**
   * Create players.
   *
   * @return the map of players
   */
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

  /**
   * Roll the dice.
   */
  public void onRoll() {
    CompositeAction currentAction = new CompositeAction();
    currentAction.addAction(new RollAction(gameBoard, currentPlayer, dice));

    String actionMessage = currentAction.execute();
    gameView.updateLogText(actionMessage);

    if (playerWon()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
    }
  }

  /**
   * Check if the player has won.
   *
   * @return true if the player has won, false otherwise
   */
  private boolean playerWon() {
    return currentPlayer.getPosition() == GameBoard.getBoardSize();
  }

  /**
   * Handle the game finish.
   */
  private void onGameFinish() {
    gameView.updateLogText("Player " + currentPlayer.getId() + " wins!");
    gameView.disableRollButton();
  }

  /**
   * Get the game board.
   *
   * @return the game board
   */
  public GameBoard getGameBoard() {
    return gameBoard;
  }
}