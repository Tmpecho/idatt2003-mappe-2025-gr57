package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;

/**
 * A generic Tile interface for any board game that is laid out in discrete tiles (spaces). In
 * Snakes and Ladders, movement is numeric. In Cluedo, movement is to adjacent tiles.
 *
 * @param <P> The type of {@link Position} used on this tile.
 */
public interface Tile<P extends Position> {

  /**
   * Called when a player arrives on this tile.
   *
   * @param player The player arriving on this tile.
   */
  void addPlayer(Player<P> player);

  /**
   * Called when a player leaves this tile.
   *
   * @param player The player leaving this tile.
   */
  void removePlayer(Player<P> player);

  /**
   * An optional identifier for debugging or referencing a tile number, name, etc.
   *
   * @return A string identifier for the tile.
   */
  String getIdentifier();

  /**
   * Adds an observer to this tile.
   *
   * @param observer The observer to add.
   */
  void addObserver(TileObserver<P> observer);

  /**
   * Removes an observer from this tile.
   *
   * @param observer The observer to remove.
   */
  void removeObserver(TileObserver<P> observer);
}
