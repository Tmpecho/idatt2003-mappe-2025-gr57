package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import java.util.Map;

public interface GameBoard {

    void addPlayersToStart(Map<Integer, Player> players);

    int getBoardSize();

    void setPlayerPosition(Player player, int position);
}
