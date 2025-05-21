package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Cards {

  private Cards() {
  }

  public static List<Suspect> shuffledSuspects(Random rng) {
    var list = new ArrayList<>(List.of(Suspect.values()));
    Collections.shuffle(list, rng);
    return list;
  }

  public static List<Weapon> shuffledWeapons(Random rng) {
    var list = new ArrayList<>(List.of(Weapon.values()));
    Collections.shuffle(list, rng);
    return list;
  }

  public static List<Room> shuffledRooms(Random rng) {
    var list = new ArrayList<>(List.of(Room.values()));
    Collections.shuffle(list, rng);
    return list;
  }
}
