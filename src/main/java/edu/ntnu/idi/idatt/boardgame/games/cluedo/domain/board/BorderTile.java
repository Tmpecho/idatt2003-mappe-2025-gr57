package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

/**
 * Represents a solid, non-walkable border tile on the Cluedo board.
 */
public final class BorderTile extends AbstractCluedoTile {

  public BorderTile(int row, int col) {
    super(row, col);
    this.walkable = false; // Border tiles are not walkable
  }

  /** Border tiles are never walkable. */
  @Override
  public boolean isWalkable() {
    return false;
  }

  @Override
  public String getIdentifier() {
    return "BorderTile(" + row + "," + col + ")";
  }
}