package edu.ntnu.idi.idatt.boardgame.core.domain.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a collection of dice.
 * Implements the {@link DiceInterface} for rolling all dice and summing their results.
 */
public final class Dice implements DiceInterface {
  /**
   * The list of individual {@link Die} objects.
   */
  private final List<Die> dice = new ArrayList<>();

  /**
   * Constructs a Dice object with a specified number of individual dice.
   *
   * @param numberOfDice The number of dice to include in this set. Must be at least 1.
   * @throws IllegalArgumentException if numberOfDice is less than 1.
   */
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

  /**
   * Gets the last rolled value of a specific die in the set.
   *
   * @param dieIndex The index of the die (0-based).
   * @return The last rolled value of the specified die.
   * @throws IllegalArgumentException if dieIndex is out of bounds.
   */
  public int getDie(int dieIndex) {
    if (dieIndex < 0 || dieIndex >= dice.size()) {
      throw new IllegalArgumentException("Die number out of bounds");
    }
    return dice.get(dieIndex).getLastRolledValue();
  }
}
