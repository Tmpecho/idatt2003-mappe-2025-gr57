package edu.ntnu.idi.idatt.boardgame.domain.common.player;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public abstract class Player {
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
  public abstract int getPosition();

  /**
   * Sets the position of the player.
   *
   * @param position the new position of the player
   */
  public abstract void setPosition(int position);

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
