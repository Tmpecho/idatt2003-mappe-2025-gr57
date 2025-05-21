package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;

/**
 * Interface for observers that monitor changes to a specific {@link Tile}.
 *
 * @param <P> The type of {@link Position} used by the tile.
 */
public interface TileObserver<P extends Position> {
  /**
   * Called when the observed tile has changed (e.g., a player entered or left).
   *
   * @param tile The tile that has changed.
   */
  void onTileChanged(Tile<P> tile);
}
