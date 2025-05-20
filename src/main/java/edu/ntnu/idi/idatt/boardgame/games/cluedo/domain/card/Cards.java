package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.*;

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

  private Cards() {}

  public static String[] getPeople() {
    return SUSPECTS.clone();
  }

  public static String[] getWeapons() {
    return WEAPONS.clone();
  }

  public static String[] getRooms() {
    return ROOMS.clone();
  }

  /** fully-shuffled 18-card draw pile (case-file removed). */
  public static List<Card> shuffledDeck(Random rnd) {
    List<Card> deck = new ArrayList<>();

    Arrays.stream(SUSPECTS).map(w -> new Card(w, CardType.SUSPECT)).forEach(deck::add);
    Arrays.stream(WEAPONS).map(w -> new Card(w, CardType.WEAPON)).forEach(deck::add);
    Arrays.stream(ROOMS).map(r -> new Card(r, CardType.ROOM)).forEach(deck::add);

    Collections.shuffle(deck, rnd);
    return deck;
  }
}
