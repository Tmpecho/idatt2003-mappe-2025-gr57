package edu.ntnu.idi.idatt.boardgame.core.engine.event;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;

public interface GameObserver<P extends Position> {
    void update(String message);

  void gameFinished(Player<P> currentPlayer);
}