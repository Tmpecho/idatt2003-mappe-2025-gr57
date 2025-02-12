package edu.idi.idatt.model;

import java.util.Random;

public class Die {
  private int lastRolledValue;
  private final Random random = new Random();

  public int roll() {
    lastRolledValue = random.nextInt(6) + 1;
    return lastRolledValue;
  }

  public int getLastRolledValue() {
    return lastRolledValue;
  }
}
