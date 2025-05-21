package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player in the Cluedo game, extending the generic {@link Player} class. Cluedo
 * players have a hand of {@link Card}s.
 */
public final class CluedoPlayer extends Player<GridPos> {

  /**
   * The list of cards held by the player.
   */
  private final List<Card> hand = new ArrayList<>();

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
   * Adds a card to the player's hand.
   *
   * @param card The {@link Card} to add.
   */
  public void addCard(Card card) {
    hand.add(card);
  }

  /**
   * Gets an unmodifiable view of the player's hand of cards.
   *
   * @return An unmodifiable list of {@link Card}s in the player's hand.
   */
  public List<Card> getHand() {
    return Collections.unmodifiableList(hand);
  }
}
