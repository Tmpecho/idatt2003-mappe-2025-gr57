package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;

public interface GameBoard<P extends Position> {

  void addPlayersToStart(Map<Integer, Player<P>> players);

  void incrementPlayerPosition(Player<P> player, int increment);

  int getBoardSize();

  void setPlayerPosition(Player<P> player, P position);
}
