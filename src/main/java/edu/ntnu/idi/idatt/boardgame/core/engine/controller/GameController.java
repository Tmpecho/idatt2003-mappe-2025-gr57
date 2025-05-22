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

  protected final GameBoard<P> gameBoard;
  protected final Dice dice;
  protected Map<Integer, Player<P>> players;
  protected Player<P> currentPlayer;
  private final List<GameObserver<P>> observers = new ArrayList<>();

  protected GameController(GameBoard<P> gameBoard, Dice dice) {
    this.gameBoard = gameBoard;
    this.dice = dice;
  }

  /**
   * Sets up players based on the provided details. Subclasses must implement this to create
   * specific player types and populate the players map. The implementation should also handle
   * setting the initial currentPlayer if the game has specific turn order rules not based on player
   * ID 1.
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
   * @param playerDetailsList The list of player configurations.
   */
  public void initializeGame(List<PlayerSetupDetails> playerDetailsList) {
    this.players = setupPlayers(playerDetailsList); // Subclass creates and maps players
    if (this.players == null || this.players.isEmpty()) {
      throw new IllegalStateException("setupPlayers must return a non-empty map of players.");
    }

    gameBoard.addPlayersToStart(this.players);

    // Default to player with ID 1 starts, if not already set by setupPlayers (e.g. Cluedo)
    if (this.currentPlayer == null) {
      this.currentPlayer = this.players.get(1); // Attempt to get player with ID 1
      if (this.currentPlayer == null && !this.players.isEmpty()) {
        // Fallback if no player with ID 1, take the first available if any
        this.currentPlayer = this.players.values().iterator().next();
      } else if (this.currentPlayer == null) { // players map is empty after all
        throw new IllegalStateException("No players available to set as current.");
      }
    }
  }

  /**
   * @deprecated Use initializeGame(List<PlayerSetupDetails>) instead.
   */
  @Deprecated
  protected abstract Map<Integer, Player<P>> createPlayers(int numberOfPlayers);

  /**
   * @deprecated Use initializeGame(List<PlayerSetupDetails>) instead.
   */
  @Deprecated
  protected void initialize(int numberOfPlayers) {
    this.players = createPlayers(numberOfPlayers);
    gameBoard.addPlayersToStart(players);
    if (this.players != null && !this.players.isEmpty()) {
      currentPlayer = players.get(1); // Default to player 1 starts
      if (currentPlayer == null) { // Fallback if ID 1 doesn't exist
        currentPlayer = players.values().iterator().next();
      }
    } else {
      throw new IllegalStateException("No players created during initialization.");
    }
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
