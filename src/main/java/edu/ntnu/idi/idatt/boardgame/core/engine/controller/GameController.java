package edu.ntnu.idi.idatt.boardgame.core.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic controller for a turn-based game.
 *
 * @param <P> concrete {@link Position} implementation used by the game
 */
public abstract class GameController<P extends Position> {
  protected final GameBoard<P> gameBoard;
  protected final Dice dice;

  protected Map<Integer, Player<P>> players;
  protected Player<P> currentPlayer;

  private final List<GameObserver<P>> observers = new ArrayList<>();

  protected GameController(GameBoard<P> gameBoard, Dice dice) {
    this.gameBoard = gameBoard;
    this.dice = dice;
  }

  /** Implementations must create the players that participate in the game. */
  protected abstract Map<Integer, Player<P>> createPlayers(int numberOfPlayers);

  /** Must be called by subclass constructor after fields are set up. */
  protected void initialize(int numberOfPlayers) {
    this.players = createPlayers(numberOfPlayers);
    gameBoard.addPlayersToStart(players);
    currentPlayer = players.get(1);
  }

  public void addObserver(GameObserver<P> observer) {
    observers.add(observer);
  }

  public void removeObserver(GameObserver<P> observer) {
    observers.remove(observer);
  }

  protected void notifyObservers(String message) {
    observers.forEach(observer -> observer.update(message));
  }

  protected void notifyGameFinished(Player<P> currentPlayer) {
    observers.forEach(observer -> observer.gameFinished(currentPlayer));
  }

  protected abstract boolean isGameOver();

  protected abstract void onGameFinish();

  protected abstract Player<P> getNextPlayer();

  public GameBoard<P> getGameBoard() {
    return gameBoard;
  }

  public abstract void saveGameState(String filePath);

  public abstract void loadGameState(String filePath);
}
