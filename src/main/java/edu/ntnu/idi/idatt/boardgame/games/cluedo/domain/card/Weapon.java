package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.Arrays;

/**
 * Represents the different weapon cards in the game of Cluedo. Each enum constant corresponds to a
 * specific weapon.
 */
public enum Weapon implements Card {
  CANDLESTICK("Candlestick"),
  KNIFE("Knife"),
  LEAD_PIPE("Lead Pipe"),
  REVOLVER("Revolver"),
  ROPE("Rope"),
  WRENCH("Wrench");

  private final String name;

  Weapon(String name) {
    this.name = name;
  }

  /**
   * Returns an array of the display names of all weapons.
   *
   * @return a String array containing the names of all weapons
   */
  public static String[] names() {
    return Arrays.stream(Weapon.values()).map(Weapon::getName).toArray(String[]::new);
  }

  @Override
  public String getName() {
    return name;
  }
}
