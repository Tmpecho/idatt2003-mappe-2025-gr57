package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for handling Cluedo cards, providing methods to obtain shuffled lists of suspects,
 * weapons, and rooms.
 */
public final class Cards {

  /**
   * Private constructor to prevent instantiation.
   */
  private Cards() {
  }

  /**
   * Returns a shuffled list of all suspects using the provided random number generator.
   *
   * @param rng The random number generator to use for shuffling.
   * @return A shuffled list of suspects.
   */
  public static List<Suspect> shuffledSuspects(Random rng) {
    ArrayList<Suspect> list = new ArrayList<>(List.of(Suspect.values()));
    Collections.shuffle(list, rng);
    return list;
  }

  /**
   * Returns a shuffled list of all weapons using the provided random number generator.
   *
   * @param rng The random number generator to use for shuffling.
   * @return A shuffled list of weapons.
   */
  public static List<Weapon> shuffledWeapons(Random rng) {
    ArrayList<Weapon> list = new ArrayList<>(List.of(Weapon.values()));
    Collections.shuffle(list, rng);
    return list;
  }

  /**
   * Returns a shuffled list of all rooms using the provided random number generator.
   *
   * @param rng The random number generator to use for shuffling.
   * @return A shuffled list of rooms.
   */
  public static List<Room> shuffledRooms(Random rng) {
    ArrayList<Room> list = new ArrayList<>(List.of(Room.values()));
    Collections.shuffle(list, rng);
    return list;
  }
}
