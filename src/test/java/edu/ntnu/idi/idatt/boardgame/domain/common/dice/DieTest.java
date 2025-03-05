package edu.ntnu.idi.idatt.boardgame.domain.common.dice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class DieTest {
  @RepeatedTest(1000)
  void testRollRange() {
    Die die = new Die();
    int value = die.roll();
    assertTrue(value >= 1 && value <= 6, "Die roll must be between 1 and 6");
  }

  @Test
  void getLastRolledValue() {
    Die die = new Die();
    int rolledValue = die.roll();
    assertEquals(rolledValue, die.getLastRolledValue());
  }
}
