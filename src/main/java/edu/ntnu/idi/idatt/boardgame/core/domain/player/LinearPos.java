package edu.ntnu.idi.idatt.boardgame.core.domain.player;

/** 1-D position.
 * @param index The linear index representing the position.
 */
public record LinearPos(int index) implements Position {
  @Override
  public String toString() {
    return "index: " + index;
  }
}
