package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class providing access to Cluedo card information and deck creation. This class defines
 * the standard sets of suspects, weapons, and rooms. It cannot be instantiated.
 */
public final class Cards {

  private static final String[] SUSPECTS = Suspect.names();
  private static final String[] WEAPONS = {
      "Candlestick", "Knife", "Lead Pipe",
      "Revolver", "Rope", "Wrench"
  };
  private static final String[] ROOMS = {
      "Kitchen",
      "Ballroom",
      "Conservatory",
      "Dining Room",
      "Billiard Room",
      "Library",
      "Lounge",
      "Hall",
      "Study"
  };

  private Cards() {
  }

  /**
   * Gets an array of all suspect names.
   *
   * @return A new array containing the names of all suspects.
   */
  public static String[] getPeople() {
    return SUSPECTS.clone();
  }

  /**
   * Gets an array of all weapon names.
   *
   * @return A new array containing the names of all weapons.
   */
  public static String[] getWeapons() {
    return WEAPONS.clone();
  }

  /**
   * Gets an array of all room names (excluding the central "Cluedo" room if considered special).
   *
   * @return A new array containing the names of all standard rooms.
   */
  public static String[] getRooms() {
    return ROOMS.clone();
  }

  /**
   * Creates a fully shuffled deck of Cluedo cards. This deck includes all suspect, weapon, and room
   * cards. The solution cards (case file) are typically removed from this deck *after* generation
   * by the game controller.
   *
   * @param rnd A {@link Random} instance to use for shuffling the deck.
   * @return A {@link List} of {@link Card} objects, shuffled.
   */
  public static List<Card> shuffledDeck(Random rnd) {
    List<Card> deck = new ArrayList<>();

    Arrays.stream(SUSPECTS).map(w -> new Card(w, CardType.SUSPECT)).forEach(deck::add);
    Arrays.stream(WEAPONS).map(w -> new Card(w, CardType.WEAPON)).forEach(deck::add);
    Arrays.stream(ROOMS).map(r -> new Card(r, CardType.ROOM)).forEach(deck::add);

    Collections.shuffle(deck, rnd);
    return deck;
  }
}
