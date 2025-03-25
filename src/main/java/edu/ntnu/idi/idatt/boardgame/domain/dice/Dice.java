package edu.ntnu.idi.idatt.boardgame.domain.dice;

import java.util.ArrayList;
import java.util.List;


public class Dice implements DiceInterface { // Class now implements the interface
  private final List<Die> dice = new ArrayList<>();

  public Dice(int numberOfDice) {
    if (numberOfDice < 1) {
      throw new IllegalArgumentException("Number of dice must be at least 1");
    }
    for (int i = 0; i < numberOfDice; i++) {
      dice.add(new Die());
    }
  }

  @Override
  public int roll() {
    int sum = 0;
    for (Die die : dice) {
      sum += die.roll();
    }
    return sum;
  }

  public int getDie(int dieIndex) {
    if (dieIndex < 0 || dieIndex >= dice.size()) {
      throw new IllegalArgumentException("Die number out of bounds");
    }
    return dice.get(dieIndex).getLastRolledValue();
  }
}