package edu.idi.idatt.model;

public class Player {
  private final String name;
  private int position;

  public Player(String name) {
    this.name = name;
    this.position = 1;
  }

  public String getName() {
    return name;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    if (position < 1 && position > GameBoard.getBoardSize()) {
      throw new IllegalArgumentException("Position must be between 0 and the furthest position on the board.");
    }
    this.position = position;
  }
}
