package edu.ntnu.idi.idatt.boardgame.domain.board;

import javafx.scene.paint.Color;

class Ladder extends Connector {
  private final int end;
  private final Color color = Color.GREEN;

  public Ladder(int start, int length) {
    super(start);
    this.end = start + length;
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
