package edu.ntnu.idi.idatt.boardgame.common.domain.board;

public interface TileObserver {
    void onTileChanged(Tile tile);
}