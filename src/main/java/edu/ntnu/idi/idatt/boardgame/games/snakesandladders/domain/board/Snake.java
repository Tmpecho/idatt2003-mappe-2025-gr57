package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import javafx.scene.paint.Color;

/**
 * Represents a snake on the Snakes and Ladders board.
 * A snake moves a player from its start (head) to a lower-numbered end (tail).
 */
public final class Snake extends Connector {
  /**
   * The tile number where the snake's tail is located.
   */
  private final int end;
  /**
   * The color used to represent snakes on the board (typically red).
   */
  private final Color color = Color.RED;

  /**
   * Constructs a Snake.
   *
   * @param start The tile number where the snake's head is located (start of the slide).
   * @param length The number of tiles the snake makes a player slide down.
   *               The end position will be {@code start - length}.
   */
  public Snake(int start, int length) {
    super(start);
    if (length <= 0) {
      throw new IllegalArgumentException("Snake length must be positive.");
    }
    this.end = start - length;
    if (this.end < 1) {
      throw new IllegalArgumentException("Snake end position cannot be less than 1.");
    }
    if (this.end >= start) {
      throw new IllegalArgumentException("Snake end must be less than start.");
    }
  }

  @Override
  public int getEnd() {
    return end;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public Object getConnectorType() {
    return "Snake";
  }
}
