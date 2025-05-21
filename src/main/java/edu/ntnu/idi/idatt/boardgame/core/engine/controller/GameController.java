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

  /**
   * The game board instance.
   */
  protected final GameBoard<P> gameBoard;
  /**
   * The dice used in the game.
   */
  protected final Dice dice;

  /**
   * A map of player IDs to {@link Player} objects.
   */
  protected Map<Integer, Player<P>> players;
  /**
   * The player whose turn it is currently.
   */
  protected Player<P> currentPlayer;

  /**
   * A list of observers monitoring game events.
   */
  private final List<GameObserver<P>> observers = new ArrayList<>();

  /**
   * Constructs a GameController.
   *
   * @param gameBoard The game board.
   * @param dice      The dice used in the game.
   */
  protected GameController(GameBoard<P> gameBoard, Dice dice) {
    this.gameBoard = gameBoard;
    this.dice = dice;
  }

  /**
   * Implementations must create the players that participate in the game.
   *
   * @param numberOfPlayers The number of players to create.
   * @return A map of player IDs to Player objects.
   */
  protected abstract Map<Integer, Player<P>> createPlayers(int numberOfPlayers);

  /**
   * Must be called by subclass constructor after fields are set up. Initializes players and places
   * them on the board.
   *
   * @param numberOfPlayers The number of players in the game.
   */
  protected void initialize(int numberOfPlayers) {
    this.players = createPlayers(numberOfPlayers);
    gameBoard.addPlayersToStart(players);
    currentPlayer = players.get(1); // Default to player 1 starts
  }

  /**
   * Adds a game observer.
   *
   * @param observer The observer to add.
   */
  public void addObserver(GameObserver<P> observer) {
    observers.add(observer);
  }

  /**
   * Removes a game observer.
   *
   * @param observer The observer to remove.
   */
  public void removeObserver(GameObserver<P> observer) {
    observers.remove(observer);
  }

  /**
   * Notifies all registered observers with an update message.
   *
   * @param message The message to send to observers.
   */
  protected void notifyObservers(String message) {
    observers.forEach(observer -> observer.update(message));
  }

  /**
   * Notifies all registered observers that the game has finished.
   *
   * @param currentPlayer The player who was current when the game finished.
   */
  protected void notifyGameFinished(Player<P> currentPlayer) {
    observers.forEach(observer -> observer.gameFinished(currentPlayer));
  }

  /**
   * Checks if the game is over.
   *
   * @return True if the game is over, false otherwise.
   */
  protected abstract boolean isGameOver();

  /**
   * Handles game finishing logic, such as notifying observers.
   */
  protected abstract void onGameFinish();

  /**
   * Determines the next player in turn order.
   *
   * @return The next player.
   */
  protected abstract Player<P> getNextPlayer();

  /**
   * Gets the game board.
   *
   * @return The game board.
   */
  public GameBoard<P> getGameBoard() {
    return gameBoard;
  }

  /**
   * Saves the current game state to the specified file path.
   *
   * @param filePath The path to save the game state to.
   */
  public abstract void saveGameState(String filePath);

  /**
   * Loads the game state from the specified file path.
   *
   * @param filePath The path to load the game state from.
   */
  public abstract void loadGameState(String filePath);
}
