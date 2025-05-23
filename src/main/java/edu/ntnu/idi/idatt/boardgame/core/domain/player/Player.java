package edu.ntnu.idi.idatt.boardgame.core.domain.player;

/**
 * Represents a player in a board game.
 *
 * @param <P> The type of {@link Position} used by this player.
 */
public class Player<P extends Position> {

  /**
   * The unique identifier for the player.
   */
  private final int id;
  /**
   * The name of the player.
   */
  private final String name;
  /**
   * The color associated with the player.
   */
  private final PlayerColor color;

  /** The current position of the player on the game board. */
  private P position;

  /**
   * Constructs a new Player.
   *
   * @param id       The unique identifier for the player.
   * @param name     The name of the player.
   * @param color    The color of the player.
   * @param startPos The starting position of the player.
   */
  public Player(int id, String name, PlayerColor color, P startPos) {
    this.id = id;
    this.name = name;
    this.position = startPos;
    this.color = color;
  }

  /**
   * Gets the player's ID.
   *
   * @return The player's ID.
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the player's name.
   *
   * @return The player's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the player's current position.
   *
   * @return The player's current position.
   */
  public P getPosition() {
    return position;
  }

  /**
   * Sets the player's current position.
   *
   * @param position The new position for the player.
   */
  public void setPosition(P position) {
    this.position = position;
  }

  /**
   * Gets the player's color.
   *
   * @return The player's color.
   */
  public PlayerColor getColor() {
    return color;
  }

  /**
   * Returns a string representation of the player, including ID, name, position, and color.
   *
   * @return A string representation of the player.
   */
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
