package edu.ntnu.idi.idatt.boardgame.core.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.GameObserver;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
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
   * Sets up players based on the provided details. Subclasses must implement this to create
   * specific player types and populate the players map. The implementation should also handle
   * setting the initial currentPlayer if the game has specific turn order rules not based on player
   * ID 1. For loading scenarios, this method might be called with an empty or null list, and
   * players will be populated later by loadGameState.
   *
   * @param playerDetailsList List of {@link PlayerSetupDetails} for each player.
   * @return A map of player IDs to Player objects.
   */
  protected abstract Map<Integer, Player<P>> setupPlayers(
      List<PlayerSetupDetails> playerDetailsList);

  /**
   * Initializes the game with a list of player setup details. This method should be called by
   * subclass constructors after super() and after any game-specific board/dice setup.
   *
   * @param playerDetailsList The list of player configurations. Can be null or empty for loading.
   */
  public void initializeGame(List<PlayerSetupDetails> playerDetailsList) {
    this.players = setupPlayers(playerDetailsList);
    if (this.players == null) {
      throw new IllegalStateException("setupPlayers must return a non-null map of players.");
    }

    gameBoard.addPlayersToStart(this.players);

    if (this.currentPlayer == null && !this.players.isEmpty()) {
      this.currentPlayer = this.players.get(1);
      if (this.currentPlayer == null) {
        // Fallback if no player with ID 1, take the first available if any
        this.currentPlayer = this.players.values().iterator().next();
      }
    }
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
