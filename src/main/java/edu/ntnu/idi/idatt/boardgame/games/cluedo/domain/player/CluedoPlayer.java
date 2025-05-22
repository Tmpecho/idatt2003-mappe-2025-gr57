package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Random;

public final class CluedoPlayer extends Player<GridPos> {
  private final Set<Suspect> suspectHand = new HashSet<>();
  private final Set<Weapon> weaponHand = new HashSet<>();
  private final Set<Room> roomHand = new HashSet<>();
  private final Map<Suspect, Boolean> suspectNotes = new EnumMap<>(Suspect.class);
  private final Map<Weapon, Boolean> weaponNotes = new EnumMap<>(Weapon.class);
  private final Map<Room, Boolean> roomNotes = new EnumMap<>(Room.class);

  public CluedoPlayer(int id, String name, PlayerColor color, GridPos startPos) {
    super(id, name, color, startPos);

    Arrays.stream(Suspect.values()).forEach(suspect -> suspectNotes.put(suspect, false));
    Arrays.stream(Weapon.values()).forEach(weapon -> weaponNotes.put(weapon, false));
    Arrays.stream(Room.values()).forEach(room -> roomNotes.put(room, false));
  }

  public void addCard(Suspect suspect) {
    suspectHand.add(suspect);
  }

  public void addCard(Weapon weapon) {
    weaponHand.add(weapon);
  }

  public void addCard(Room room) {
    roomHand.add(room);
  }

  public boolean hasCard(Suspect suspect) {
    return suspectHand.contains(suspect);
  }

  public boolean hasCard(Weapon weapon) {
    return weaponHand.contains(weapon);
  }

  public boolean hasCard(Room room) {
    return roomHand.contains(room);
  }

  /** pick one at random if there are multiple options */
  public Card showOneOf(Collection<Card> options, Random rng) {
    List<Card> matches =
        options.stream()
            .filter(
                card -> {
                  if (card instanceof Suspect) {
                    return suspectHand.contains(card);
                  }
                  if (card instanceof Weapon) {
                    return weaponHand.contains(card);
                  }
                  if (card instanceof Room) {
                    return roomHand.contains(card);
                  }
                  LoggingNotification.error(
                      this.getClass().getName(), "Unknown card type: " + card);
                  throw new IllegalArgumentException("Unknown card type: " + card.getClass());
                })
            .toList();
    if (matches.isEmpty()) {
      LoggingNotification.error(this.getClass().getName(), "No cards to show");
      throw new IllegalArgumentException("No cards to show");
    }
    return matches.get(rng.nextInt(matches.size()));
  }

  public boolean isSuspectNoted(Suspect s) {
    return suspectNotes.get(s);
  }

  public void setSuspectNoted(Suspect s, boolean v) {
    suspectNotes.put(s, v);
  }

  public boolean isWeaponNoted(Weapon w) {
    return weaponNotes.get(w);
  }

  public void setWeaponNoted(Weapon w, boolean v) {
    weaponNotes.put(w, v);
  }

  public boolean isRoomNoted(Room r) {
    return roomNotes.get(r);
  }

  public void setRoomNoted(Room r, boolean v) {
    roomNotes.put(r, v);
  }
}
