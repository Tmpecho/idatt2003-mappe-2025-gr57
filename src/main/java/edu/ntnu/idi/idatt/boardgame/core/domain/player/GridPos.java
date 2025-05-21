package edu.ntnu.idi.idatt.boardgame.core.domain.player;

/** 2-D position.
 * @param row The row coordinate.
 * @param col The column coordinate.
 */
public record GridPos(int row, int col) implements Position {
  @Override
  public String toString() {
    return "[" + row + ", " + col + "]";
  }
}
