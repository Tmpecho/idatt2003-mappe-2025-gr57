package edu.idi.idatt;

import java.util.Random;


public class Die {
	private int lastRolledValue;

	public int roll() {
		Random random = new Random();
		int randomNumber = random.nextInt(6) + 1;
		lastRolledValue = randomNumber;
		return randomNumber;
	}

	public int getLastRolledValue() {
		return lastRolledValue;
	}
}
