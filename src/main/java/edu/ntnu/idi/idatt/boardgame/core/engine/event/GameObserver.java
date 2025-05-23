package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;

/**
 * Interface for observers that monitor game events.
 *
 * @param <P> The type of {@link Position} used in the game.
 */
public interface GameObserver<P extends Position> {

  /**
   * Called when a general game update occurs.
   *
   * @param message A message describing the update.
   */
  void update(String message);

  /**
   * Called when the game has finished.
   *
   * @param currentPlayer The player who was current when the game finished (often the winner).
   */
  void gameFinished(Player<P> currentPlayer);
}
