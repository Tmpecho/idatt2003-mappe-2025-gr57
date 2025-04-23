package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;

public interface GameBoard {

    void addPlayersToStart(Map<Integer, Player> players);

    void incrementPlayerPosition(Player player, int increment);

    int getBoardSize();

    void setPlayerPosition(Player player, int position);
}
