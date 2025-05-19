package edu.ntnu.idi.idatt.boardgame.core.domain.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class Dice implements DiceInterface {
  private final List<Die> dice = new ArrayList<>();

  public Dice(int numberOfDice) {
    if (numberOfDice < 1) {
      throw new IllegalArgumentException("Number of dice must be at least 1");
    }
	  IntStream.range(0, numberOfDice).mapToObj(i -> new Die()).forEach(dice::add);
  }

  @Override
  public int roll() {
	  return dice.stream().mapToInt(Die::roll).sum();
  }

  public int getDie(int dieIndex) {
    if (dieIndex < 0 || dieIndex >= dice.size()) {
      throw new IllegalArgumentException("Die number out of bounds");
    }
    return dice.get(dieIndex).getLastRolledValue();
  }
}