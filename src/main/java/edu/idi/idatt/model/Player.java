package edu.idi.idatt.model;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Paint;

public class Player {
  private final String name;
  private int position;
  private Circle icon;

  /**
   * Creates a new player with the given name and color.
   *
   * @param name the name of the player
   * @param color the color of the player
   */
  public Player(String name, Paint color) {
    this.name = name;
    this.position = 1;
    this.icon = new Circle(15);
    this.icon.setFill(color);
  }

  /**
   * Returns the name of the player.
   *
   * @return the name of the player
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the position of the player.
   *
   * @return the position of the player
   */
  public int getPosition() {
    return position;
  }

  /**
   * Increments the position of the player by the given increment.
   *
   * @param increment the amount to increment the position by
   * @return the new position of the player
   */
  public int incrementPosition(int increment) {
    int newPosition = position + increment;
    if (newPosition > GameBoard.getBoardSize()) {
      // bounce back
      newPosition = GameBoard.getBoardSize() - (newPosition - GameBoard.getBoardSize());
    }
    position = newPosition;
    return position;
  }

  /**
   * Sets the position of the player.
   *
   * @param position the new position of the player
   */
  public void setPosition(int position) {
    if (position < 1 && position > GameBoard.getBoardSize()) {
      throw new IllegalArgumentException(
          "Position must be between 1 and the furthest position on the board.");
    }
    this.position = position;
  }

  /**
   * Returns the icon of the player.
   *
   * @return the icon of the player
   */
  public Circle getIcon() {
    return icon;
  }
}
