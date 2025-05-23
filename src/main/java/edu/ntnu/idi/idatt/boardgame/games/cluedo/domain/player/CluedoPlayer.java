package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a player in the Cluedo game, extending the generic {@link Player} class. Cluedo
 * players have a hand of {@link Card}s.
 */
public final class CluedoPlayer extends Player<GridPos> {

  private final Set<Suspect> suspectHand = new HashSet<>();
  private final Set<Weapon> weaponHand = new HashSet<>();
  private final Set<Room> roomHand = new HashSet<>();
  private final Map<Suspect, Boolean> suspectNotes = new EnumMap<>(Suspect.class);
  private final Map<Weapon, Boolean> weaponNotes = new EnumMap<>(Weapon.class);
  private final Map<Room, Boolean> roomNotes = new EnumMap<>(Room.class);

  /**
   * Constructs a new CluedoPlayer.
   *
   * @param id The unique identifier for the player.
   * @param name The name of the player (typically the suspect name).
   * @param color The {@link PlayerColor} associated with the player/suspect.
   * @param startPos The starting {@link GridPos} of the player on the board.
   */
  public CluedoPlayer(int id, String name, PlayerColor color, GridPos startPos) {
    super(id, name, color, startPos);

    Arrays.stream(Suspect.values()).forEach(suspect -> suspectNotes.put(suspect, false));
    Arrays.stream(Weapon.values()).forEach(weapon -> weaponNotes.put(weapon, false));
    Arrays.stream(Room.values()).forEach(room -> roomNotes.put(room, false));
  }

  /**
   * Adds a suspect card to the player's hand.
   *
   * @param suspect the suspect card to add
   */
  public void addCard(Suspect suspect) {
    suspectHand.add(suspect);
  }

  /**
   * Adds a weapon card to the player's hand.
   *
   * @param weapon the weapon card to add
   */
  public void addCard(Weapon weapon) {
    weaponHand.add(weapon);
  }

  /**
   * Adds a room card to the player's hand.
   *
   * @param room the room card to add
   */
  public void addCard(Room room) {
    roomHand.add(room);
  }

  /**
   * Checks if the player has the specified suspect card.
   *
   * @param suspect the suspect card to check
   * @return true if the player has the suspect card, false otherwise
   */
  public boolean hasCard(Suspect suspect) {
    return suspectHand.contains(suspect);
  }

  /**
   * Checks if the player has the specified weapon card.
   *
   * @param weapon the weapon card to check
   * @return true if the player has the weapon card, false otherwise
   */
  public boolean hasCard(Weapon weapon) {
    return weaponHand.contains(weapon);
  }

  /**
   * Checks if the player has the specified room card.
   *
   * @param room the room card to check
   * @return true if the player has the room card, false otherwise
   */
  public boolean hasCard(Room room) {
    return roomHand.contains(room);
  }

  /**
   * Checks if a specific suspect is noted in the player' suspect notes.
   *
   * @param suspect the suspect to check
   * @return true if the suspect is noted, false otherwise
   */
  public boolean isSuspectNoted(Suspect suspect) {
    return suspectNotes.get(suspect);
  }

  /**
   * Updates the suspect notes for the player, marking the given suspect as either noted or not.
   *
   * @param suspect The suspect to update in the player's notes.
   * @param v A boolean value where {@code true} marks the suspect as noted, and {@code false} marks
   *     the suspect as not noted.
   */
  public void setSuspectNoted(Suspect suspect, boolean v) {
    suspectNotes.put(suspect, v);
  }

  /**
   * Checks if a specific weapon is noted in the player' weapon notes.
   *
   * @param weapon the weapon to check
   * @return true if the weapon is noted, false otherwise
   */
  public boolean isWeaponNoted(Weapon weapon) {
    return weaponNotes.get(weapon);
  }

  /**
   * Updates the weapon notes for the player, marking the given weapon as either noted or not.
   *
   * @param weapon The weapon to update in the player's notes.
   * @param v A boolean value where {@code true} marks the weapon as noted, and {@code false} marks
   *     the weapon as not noted.
   */
  public void setWeaponNoted(Weapon weapon, boolean v) {
    weaponNotes.put(weapon, v);
  }

  /**
   * Checks if a specific room is noted in the player' room notes.
   *
   * @param room the room to check
   * @return true if the room is noted, false otherwise
   */
  public boolean isRoomNoted(Room room) {
    return roomNotes.get(room);
  }

  /**
   * Updates the room notes for the player, marking the given room as either noted or not.
   *
   * @param room The room to update in the player's notes.
   * @param v A boolean value where {@code true} marks the room as noted, and {@code false} marks
   *     the room as not noted.
   */
  public void setRoomNoted(Room room, boolean v) {
    roomNotes.put(room, v);
  }
}
