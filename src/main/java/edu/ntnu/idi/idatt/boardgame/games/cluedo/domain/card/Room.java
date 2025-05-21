package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.Arrays;

public enum Room implements Card {
  KITCHEN("Kitchen"),
  BALLROOM("Ballroom"),
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

  public static String[] names() {
    return Arrays.stream(Room.values()).map(Room::getName).toArray(String[]::new);
  }

  public String getName() {
    return name;
  }
}
