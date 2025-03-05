package edu.ntnu.idi.idatt.boardgame.controller.common;

import edu.ntnu.idi.idatt.boardgame.domain.common.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.domain.games.snakesAndLadders.player.SnakesAndLaddersPlayer;
import edu.ntnu.idi.idatt.boardgame.view.GameView;

/** The GameController class. */
public abstract class GameController<P extends Player> {
  private GameView gameView;
  private final P currentPlayer;

  public GameController() {
    currentPlayer = null;
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
  protected abstract SnakesAndLaddersPlayer getNextPlayer();

  /** Handle the game finish. */
  protected void onGameFinish() {
    if (currentPlayer != null) {
      gameView.updateLogText("Player " + currentPlayer.getId() + " wins!");
    }
    gameView.disableRollButton();
  }

  public abstract void startGame();

  /** Do action on roll. */
  public abstract void onRoll();

  /**
   * Get the game board.
   *
   * @return the game board
   */
  public abstract GameBoard getGameBoard();
}
