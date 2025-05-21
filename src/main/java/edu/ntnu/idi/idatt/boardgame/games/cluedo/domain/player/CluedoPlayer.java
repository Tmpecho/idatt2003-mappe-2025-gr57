package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents a player in the Cluedo game, extending the generic {@link Player} class. Cluedo
 * players have a hand of {@link Card}s.
 */
public final class CluedoPlayer extends Player<GridPos> {

  private final Set<Suspect> suspectHand = new HashSet<>();
  private final Set<Weapon> weaponHand = new HashSet<>();
  private final Set<Room> roomHand = new HashSet<>();

  /**
   * Constructs a new CluedoPlayer.
   *
   * @param id       The unique identifier for the player.
   * @param name     The name of the player (typically the suspect name).
   * @param color    The {@link PlayerColor} associated with the player/suspect.
   * @param startPos The starting {@link GridPos} of the player on the board.
   */
  public CluedoPlayer(int id, String name, PlayerColor color, GridPos startPos) {
    super(id, name, color, startPos);
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
   * Selects and returns one card from the given collection of options that this player holds in
   * their hand. If multiple matching cards are found, one is chosen at random using the provided
   * {@link Random} instance.
   *
   * @param options the collection of cards to check against the player's hand
   * @param rng     the random number generator used to select a card if multiple matches are found
   * @return a card from the player's hand that matches one of the options
   * @throws IllegalArgumentException if none of the options are in the player's hand or if an
   *                                  unknown card type is encountered
   */
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
}
