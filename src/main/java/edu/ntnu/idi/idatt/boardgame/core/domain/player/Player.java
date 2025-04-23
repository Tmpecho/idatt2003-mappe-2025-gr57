package edu.ntnu.idi.idatt.boardgame.core.domain.player;

public class Player {
  private final int id;
  private final String name;
  private int position;
  private final PlayerColor color;

  public Player(int id, String name, PlayerColor color) {
    this.id = id;
    this.name = name;
    this.position = 1;
    this.color = color;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public PlayerColor getColor() {
    return color;
  }

  public String toString() {
    return "Player{id=" + id + ", name=" + name + ", position=" + position + ", color=" + color + "}";
  }
}