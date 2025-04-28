package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;

public interface TileObserver {
  void onTileChanged(Tile<LinearPos> tile);
}