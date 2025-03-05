package edu.ntnu.idi.idatt.boardgame.domain.games.snakesAndLadders.board;

import javafx.scene.paint.Color;

public class Snake extends Connector {
  private final int end;
  private final Color color = Color.RED;

  public Snake(int start, int length) {
    super(start);
    this.end = start - length;
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
