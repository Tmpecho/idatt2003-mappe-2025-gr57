package edu.idi.idatt;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        int numberOfDice = 2;
        Dice dice = new Dice(numberOfDice);

        System.out.println("Threw " + numberOfDice + " dice and got " + dice.roll() + ".");
    }
}
