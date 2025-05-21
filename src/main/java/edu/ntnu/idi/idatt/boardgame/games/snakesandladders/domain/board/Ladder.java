package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import javafx.scene.paint.Color;

/**
 * Represents a ladder on the Snakes and Ladders board. A ladder moves a player from its start
 * (bottom) to a higher-numbered end (top).
 */
public final class Ladder extends Connector {

  /**
   * The tile number where the ladder's top is located.
   */
  private final int end;
  /**
   * The color used to represent ladders on the board (typically green).
   */
  private final Color color = Color.GREEN;

  /**
   * Constructs a Ladder.
   *
   * @param start  The tile number where the ladder's bottom is located (start of the climb).
   * @param length The number of tiles the ladder makes a player climb up. The end position will be
   *               {@code start + length}.
   */
  public Ladder(int start, int length) {
    super(start);
    if (length <= 0) {
      throw new IllegalArgumentException("Ladder length must be positive.");
    }
    this.end = start + length;
    if (this.end <= start) {
      throw new IllegalArgumentException("Ladder end must be greater than start.");
    }
    // Assuming board size is checked elsewhere if 'end' can exceed it.
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
    return "Ladder";
  }
}
