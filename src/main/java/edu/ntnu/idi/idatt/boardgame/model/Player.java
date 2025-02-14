package edu.ntnu.idi.idatt.boardgame.model;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class Player {
  private final int id;
  private int position;
  private final Circle icon;

  /**
   * Creates a new player with the given id and color.
   *
   * @param id the id of the player
   * @param color the color of the player
   */
  public Player(int id, Paint color) {
    this.id = id;
    this.position = 1;
    this.icon = new Circle(7);
    this.icon.setFill(color);
  }

  /**
   * Returns the name of the player.
   *
   * @return the name of the player
   */
  public int getId() {
    return id;
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
    return newPosition;
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

  @Override
  public String toString() {
    return "Player{" + "id=" + id + ", position=" + position + ", icon=" + icon + '}';
  }
}
