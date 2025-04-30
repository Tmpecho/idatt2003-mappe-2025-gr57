package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;

public interface TileObserver {
  void onTileChanged(Tile<?> tile);
}
