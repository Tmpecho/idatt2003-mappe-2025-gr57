package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import java.util.Map;

/**
 * Represents the game board.
 *
 * @param <P> the type of {@link Position} used on this game board.
 */
public interface GameBoard<P extends Position> {

  /**
   * Adds players to their starting positions on the board.
   *
   * @param players A map of player IDs to {@link Player} objects.
   */
  void addPlayersToStart(Map<Integer, Player<P>> players);

  /**
   * Gets the size of the board (e.g., number of tiles). The exact meaning of "size" can vary
   * depending on the game.
   *
   * @return The size of the board.
   */
  int getBoardSize();

  /**
   * Sets the position of a given player on the board. This method should handle removing the player
   * from their old tile and adding them to the new tile.
   *
   * @param player   The player whose position is to be set.
   * @param position The new position for the player.
   */
  void setPlayerPosition(Player<P> player, P position);
}
