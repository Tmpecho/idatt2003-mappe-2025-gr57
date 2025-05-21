package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.Arrays;

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

  public static String[] names() {
    return Arrays.stream(Weapon.values()).map(Weapon::getName).toArray(String[]::new);
  }

  @Override
  public String getName() {
    return name;
  }
}
