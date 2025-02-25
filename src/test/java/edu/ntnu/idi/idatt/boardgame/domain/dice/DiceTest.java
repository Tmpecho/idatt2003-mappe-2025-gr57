package edu.ntnu.idi.idatt.boardgame.domain.dice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class DiceTest {
  @RepeatedTest(1000)
  void testDiceSumWithinBounds() {
    int numberOfDice = 3;
    Dice dice = new Dice(numberOfDice);
    int sum = dice.roll();
    assertTrue(
        sum >= numberOfDice && sum <= 6 * numberOfDice,
        "Sum of dice must be between " + numberOfDice + " and " + (6 * numberOfDice));
  }

  @Test
  void testGetDie() {
    Dice dice = new Dice(2);
    int value = dice.roll();
    assertEquals(value, dice.getDie(0) + dice.getDie(1));
  }

  @Test
  void testGetDieOutOfBounds() {
    Dice dice = new Dice(2);
    assertThrows(IllegalArgumentException.class, () -> dice.getDie(-1));
    assertThrows(IllegalArgumentException.class, () -> dice.getDie(2));
  }
}
