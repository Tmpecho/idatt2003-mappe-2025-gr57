package edu.ntnu.idi.idatt.boardgame.controller.games.snakesAndLadders;

import edu.ntnu.idi.idatt.boardgame.action.common.CompositeAction;
import edu.ntnu.idi.idatt.boardgame.action.common.RollAction;
import edu.ntnu.idi.idatt.boardgame.controller.common.GameController;
import edu.ntnu.idi.idatt.boardgame.domain.common.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.games.snakesAndLadders.player.SnakesAndLaddersPlayer;
import edu.ntnu.idi.idatt.boardgame.view.GameView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.paint.Color;

public class SnakesAndLaddersController extends GameController {
  private final GameBoard gameBoard = new GameBoard();
  private GameView gameView;
  private final int numberOfPlayers;
  private final Map<Integer, SnakesAndLaddersPlayer> players;
  private SnakesAndLaddersPlayer currentPlayer;
  private final List<Color> playerColors =
      List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);
  private final Dice dice = new Dice(2);

  /** Constructor for GameController. */
  public SnakesAndLaddersController(int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
    this.players = createPlayers();
    this.currentPlayer = players.get(1);
  }

  /**
   * Create players.
   *
   * @return the map of players
   */
  private Map<Integer, SnakesAndLaddersPlayer> createPlayers() {
    Map<Integer, SnakesAndLaddersPlayer> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              SnakesAndLaddersPlayer player =
                  new SnakesAndLaddersPlayer(playerId, playerColors.get(playerId - 1));
              players.put(playerId, player);
            });
    return players;
  }

    /**
     * Get the next player.
     *
     * @return the next player
     */
    protected SnakesAndLaddersPlayer getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  /** Roll the dice. */
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

  public void startGame() {
    gameBoard.addPlayersToStart(players);
  }

  public void setGameView(GameView gameView) {
    this.gameView = gameView;
  }

  @Override
  public GameBoard getGameBoard() {
    return gameBoard;
  }
}
