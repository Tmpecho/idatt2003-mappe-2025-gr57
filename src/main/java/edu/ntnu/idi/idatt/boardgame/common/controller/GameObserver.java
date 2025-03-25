package edu.ntnu.idi.idatt.boardgame.common.controller;

public interface GameObserver {
    void update(String message);
    void gameFinished(int winnerId);
}