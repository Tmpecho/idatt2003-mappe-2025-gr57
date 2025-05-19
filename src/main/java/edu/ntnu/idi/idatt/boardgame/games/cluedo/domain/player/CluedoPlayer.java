package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CluedoPlayer extends Player<GridPos> {
  private final List<Card> hand = new ArrayList<>();

  public CluedoPlayer(int id, String name, PlayerColor color, GridPos startPos) {
    super(id, name, color, startPos);
  }

  public void addCard(Card card) {
    hand.add(card);
  }

  public List<Card> getHand() {
    return Collections.unmodifiableList(hand);
  }
}
