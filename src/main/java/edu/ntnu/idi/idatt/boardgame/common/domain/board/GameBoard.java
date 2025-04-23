package edu.ntnu.idi.idatt.boardgame.common.domain.board;

import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import javafx.scene.Node;

public interface GameBoard {

    void addPlayersToStart(Map<Integer, Player> players);

    void incrementPlayerPosition(Player player, int increment);

    int getBoardSize();

    Node getNode();

    void setPlayerPosition(Player player, int position);
}
