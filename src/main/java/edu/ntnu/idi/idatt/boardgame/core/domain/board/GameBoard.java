package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import java.util.Map;

public interface GameBoard<P extends Position> {

  void addPlayersToStart(Map<Integer, Player<P>> players);

  int getBoardSize();

  void setPlayerPosition(Player<P> player, P position);
}
