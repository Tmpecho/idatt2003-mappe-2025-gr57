package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

/**
 * A single walkable square on the Cluedo board.
 */
public final class CorridorTile extends AbstractCluedoTile {

  /**
   * Constructs a CorridorTile at the specified row and column. Corridor tiles are typically
   * walkable by default.
   *
   * @param row The row coordinate of the corridor tile.
   * @param col The column coordinate of the corridor tile.
   */
  public CorridorTile(int row, int col) {
    super(row, col);
    // Walkable is true by default in AbstractCluedoTile constructor for this type
  }

}
