package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;

public interface GameObserver {
    void update(String message);
    void gameFinished(Player currentPlayer);
}