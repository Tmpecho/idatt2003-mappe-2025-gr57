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

  void addPlayer(Player<P> player);

  void removePlayer(Player<P> player);

  String getIdentifier();

  void addObserver(TileObserver<P> observer); // Generic type parameter here

  void removeObserver(TileObserver<P> observer); // Generic type parameter here
}
