package edu.ntnu.idi.idatt.boardgame.core.domain.board;

import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import javafx.scene.Node;

public interface GameBoard {

    void addPlayersToStart(Map<Integer, Player> players);

    void incrementPlayerPosition(Player player, int increment);

    int getBoardSize();

    Node getNode();

    void setView(Node view);

    void setPlayerPosition(Player player, int position);
}
