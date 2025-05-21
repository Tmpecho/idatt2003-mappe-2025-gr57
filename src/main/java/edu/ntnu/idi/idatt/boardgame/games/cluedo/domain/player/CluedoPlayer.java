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

  /**
   * pick one at random if there are multiple options
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
