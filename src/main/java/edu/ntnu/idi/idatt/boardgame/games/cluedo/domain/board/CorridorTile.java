package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

/** A single walkable square on the Cluedo board. */
public final class CorridorTile extends AbstractCluedoTile {

  public CorridorTile(int row, int col) {
    super(row, col);
  }

  /** Corridor squares are always enterable. */
  public boolean isWalkable() {
    return true;
  }
}
