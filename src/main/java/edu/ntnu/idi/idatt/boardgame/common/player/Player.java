package edu.ntnu.idi.idatt.boardgame.common.player;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class Player {
  private final int id;
  private int position;
  private final Circle icon;

  public Player(int id, Paint color) {
    this.id = id;
    this.position = 1;
    this.icon = new Circle(7);
    this.icon.setFill(color);
  }

  public int getId() {
    return id;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public Circle getIcon() {
    return icon;
  }

  @Override
  public String toString() {
    return "Player{" + "id=" + id + ", position=" + position + ", icon=" + icon + '}';
  }
}