package edu.ntnu.idi.idatt.boardgame.common.domain.board;

import edu.ntnu.idi.idatt.boardgame.common.player.Player;

/**
 * A generic Tile interface for any board game that is laid out in discrete tiles (spaces). In
 * Snakes and Ladders, movement is numeric. In Cluedo, movement is to adjacent tiles.
 */
public interface Tile {
  /** Called when a player arrives on this tile. */
  void addPlayer(Player player);

  /** Called when a player leaves this tile. */
  void removePlayer(Player player);

  /** An optional identifier for debugging or referencing a tile number, name, etc. */
  String getIdentifier();

  /** Adds an observer to this tile. */
  void addObserver(TileObserver observer);

  /** Removes an observer from this tile. */
  void removeObserver(TileObserver observer);
}
