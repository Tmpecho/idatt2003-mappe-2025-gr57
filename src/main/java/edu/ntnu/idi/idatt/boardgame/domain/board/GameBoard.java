package edu.ntnu.idi.idatt.boardgame.domain.board;

import javafx.scene.Node;
import edu.ntnu.idi.idatt.boardgame.domain.player.Player;
import java.util.Map;

public interface GameBoard {
    void addPlayersToStart(Map<Integer, Player> players);
    String incrementPlayerPosition(Player player, int increment);
    int getBoardSize();
    Node getNode();
}