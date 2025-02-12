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

  public int incrementPosition(int increment) {
    int newPosition = position + increment;
    if (newPosition > GameBoard.getBoardSize()) {
      // bounce back
      newPosition = GameBoard.getBoardSize() - (newPosition - GameBoard.getBoardSize());
    }
    position = newPosition;
    return position;
  }

  public void setPosition(int position) {
    if (position < 1 && position > GameBoard.getBoardSize()) {
      throw new IllegalArgumentException(
          "Position must be between 1 and the furthest position on the board.");
    }
    this.position = position;
  }
}
