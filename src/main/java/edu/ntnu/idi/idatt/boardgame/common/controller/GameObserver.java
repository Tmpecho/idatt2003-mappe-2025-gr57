package edu.ntnu.idi.idatt.boardgame.common.controller;

import edu.ntnu.idi.idatt.boardgame.common.player.Player;

public interface GameObserver {
    void update(String message);
    void gameFinished(Player currentPlayer);
}