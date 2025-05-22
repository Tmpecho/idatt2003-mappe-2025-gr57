package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import javafx.scene.paint.Color;

/**
 * Represents a connector between two tiles on the game board.
 */
public abstract class Connector {

  private final int start;

  /**
   * Constructs a connector. For snakes (red), start is the head (largest tile) and end is start –
   * length. For ladders (green), start is the bottom (smallest tile) and end is start + length.
   *
   * @param start The starting tile number of the connector.
   */
  public Connector(int start) {
    this.start = start;
  }

  /**
   * Gets the starting tile number of this connector.
   *
   * @return The start tile number.
   */
  public int getStart() {
    return start;
  }

  /**
   * Gets the ending tile number of this connector. For a snake, this is the tail. For a ladder,
   * this is the top.
   *
   * @return The end tile number.
   */
  public abstract int getEnd();

  /**
   * Gets the color associated with this type of connector (e.g., red for snakes, green for
   * ladders).
   *
   * @return The JavaFX {@link Color} of the connector.
   */
  public abstract Color getColor();

  /**
   * Gets a string representation of the connector type (e.g., "Snake", "Ladder").
   *
   * @return The type of the connector as an Object (typically String).
   */
  public abstract Object getConnectorType();
}
