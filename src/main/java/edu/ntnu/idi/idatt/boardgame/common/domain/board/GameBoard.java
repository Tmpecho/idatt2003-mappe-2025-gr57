package edu.ntnu.idi.idatt.boardgame.common.domain.board;

import javafx.scene.Node;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import java.util.Map;

public interface GameBoard {
    void addPlayersToStart(Map<Integer, Player> players);
    void incrementPlayerPosition(Player player, int increment);
    int getBoardSize();
    Node getNode();
}