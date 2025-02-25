package edu.ntnu.idi.idatt.boardgame.domain.board;

import javafx.scene.paint.Color;

/** Represents a connector between two tiles on the game board. */
abstract class Connector {
  private final int start;

  /**
   * Constructs a connector. For snakes (red), start is the head (largest tile) and end is start –
   * length. For ladders (green), start is the bottom (smallest tile) and end is start + length.
   */
  public Connector(int start) {
    this.start = start;
  }

  public int getStart() {
    return start;
  }

  public abstract int getEnd();

  public abstract Color getColor();

  public abstract Object getConnectorType();
}
