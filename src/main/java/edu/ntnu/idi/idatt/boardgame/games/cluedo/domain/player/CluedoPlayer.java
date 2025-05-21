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

public final class CluedoPlayer extends Player<GridPos> {
  private final Set<Suspect> suspectHand = new HashSet<>();
  private final Set<Weapon> weaponHand = new HashSet<>();
  private final Set<Room> roomHand = new HashSet<>();

  public CluedoPlayer(int id, String name, PlayerColor color, GridPos startPos) {
    super(id, name, color, startPos);
  }

  public void addCard(Suspect s) {
    suspectHand.add(s);
  }

  public void addCard(Weapon w) {
    weaponHand.add(w);
  }

  public void addCard(Room r) {
    roomHand.add(r);
  }

  public boolean hasCard(Suspect s) {
    return suspectHand.contains(s);
  }

  public boolean hasCard(Weapon w) {
    return weaponHand.contains(w);
  }

  public boolean hasCard(Room r) {
    return roomHand.contains(r);
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
                  LoggingNotification.error(this.getName(), "Unknown card type: " + card);
                  throw new IllegalArgumentException("Unknown card type: " + card.getClass());
                })
            .toList();
    return matches.get(rng.nextInt(matches.size()));
  }
}
