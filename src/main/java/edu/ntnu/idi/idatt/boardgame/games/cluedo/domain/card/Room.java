package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.Arrays;

/**
 * Represents the different room cards in the game of Cluedo. Each enum constant corresponds to a
 * specific room on the board.
 */
public enum Room implements Card {
  KITCHEN("Kitchen"),
  BALLROOM("Ball Room"),
  CONSERVATORY("Conservatory"),
  DINING_ROOM("Dining Room"),
  BILLIARD_ROOM("Billiard Room"),
  LIBRARY("Library"),
  LOUNGE("Lounge"),
  HALL("Hall"),
  STUDY("Study");

  private final String name;

  Room(String name) {
    this.name = name;
  }

  /**
   * Returns an array of the display names of all rooms.
   *
   * @return a String array containing the names of all rooms
   */
  public static String[] names() {
    return Arrays.stream(Room.values()).map(Room::getName).toArray(String[]::new);
  }

  /**
   * Retrieves a {@link Room} enum constant by its display name.
   *
   * @param name the display name of the room to retrieve
   * @return the {@link Room} corresponding to the given display name
   * @throws IllegalArgumentException if no room matches the given display name
   */
  public static Room fromDisplayName(String name) {
    return Arrays.stream(values())
        .filter(room -> room.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown room: " + name));
  }

  @Override
  public String getName() {
    return name;
  }
}
