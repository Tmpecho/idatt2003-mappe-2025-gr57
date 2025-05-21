package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

/**
 * Represents a solid, non-walkable border tile on the Cluedo board.
 */
public final class BorderTile extends AbstractCluedoTile {

  /**
   * Constructs a BorderTile at the specified row and column.
   *
   * @param row The row coordinate of the border tile.
   * @param col The column coordinate of the border tile.
   */
  public BorderTile(int row, int col) {
    super(row, col);
    this.walkable = false; // Border tiles are not walkable
  }

  /** Border tiles are never walkable.
   * @return always false.
   */
  @Override
  public boolean isWalkable() {
    return false;
  }

  @Override
  public String getIdentifier() {
    return "BorderTile(" + row + "," + col + ")";
  }
}
