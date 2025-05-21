package edu.ntnu.idi.idatt.boardgame.core.domain.dice;

/**
 * Interface for objects that can be rolled, like a set of dice.
 */
public interface DiceInterface {
    /**
     * Performs a roll operation.
     * For a set of dice, this typically means rolling all dice and returning their sum.
     *
     * @return The result of the roll.
     */
    int roll();
}
