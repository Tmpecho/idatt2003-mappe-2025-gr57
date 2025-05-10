package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;

public interface TileObserver<P extends Position> {
  void onTileChanged(Tile<P> tile);
}