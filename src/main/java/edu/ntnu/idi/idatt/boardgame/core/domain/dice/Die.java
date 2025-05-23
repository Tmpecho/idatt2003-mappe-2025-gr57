package edu.ntnu.idi.idatt.boardgame.core.domain.dice;

import java.util.Random;

/** Represents a single six-sided die. */
public final class Die {

  private final Random random = new Random();

  /** The value of the last roll. */
  private int lastRolledValue;

  /**
   * Rolls the die and returns the result. The result is a random integer between 1 and 6
   * (inclusive). This value is also stored as the last rolled value.
   *
   * @return The value rolled on the die.
   */
  public int roll() {
    lastRolledValue = random.nextInt(6) + 1;
    return lastRolledValue;
  }

  /**
   * Gets the value of the last roll of this die. If the die has not been rolled yet, this value may
   * be 0 or uninitialized.
   *
   * @return The last rolled value.
   */
  public int getLastRolledValue() {
    return lastRolledValue;
  }
}
