package edu.ntnu.idi.idatt.boardgame.core.domain.player;

public class Player<P extends Position> {
  private final int id;
  private final String name;
  private P position;
  private final PlayerColor color;

  public Player(int id, String name, PlayerColor color, P startPos) {
    this.id = id;
    this.name = name;
    this.position = startPos;
    this.color = color;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public P getPosition() {
    return position;
  }

  public void setPosition(P position) {
    this.position = position;
  }

  public PlayerColor getColor() {
    return color;
  }

  public String toString() {
    return "Player{id="
        + id
        + ", name="
        + name
        + ", position="
        + position
        + ", color="
        + color
        + "}";
  }
}
