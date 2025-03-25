package edu.ntnu.idi.idatt.boardgame.controller;

import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GameController {
  protected final GameBoard gameBoard;
  protected final Dice dice;
  protected Map<Integer, Player> players; // No longer initialized in constructor
  protected Player currentPlayer;
  private final List<GameObserver> observers = new ArrayList<>();

  public GameController(GameBoard gameBoard, Dice dice) { // Simplified constructor
    this.gameBoard = gameBoard;
    this.dice = dice;
  }

  protected abstract Map<Integer, Player> createPlayers(int numberOfPlayers);

  // New method to handle initialization
  public void initialize(int numberOfPlayers) {
    this.players = createPlayers(numberOfPlayers);
    gameBoard.addPlayersToStart(players);
    this.currentPlayer = players.get(1);
  }

  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(GameObserver observer) {
    observers.remove(observer);
  }

  protected void notifyObservers(String message) {
    for (GameObserver observer : observers) {
      observer.update(message);
    }
  }

  protected void notifyGameFinished(int winnerId) {
    for (GameObserver observer : observers) {
      observer.gameFinished(winnerId);
    }
  }

  public void onRoll() {
    String actionMessage = performTurn();
    notifyObservers(actionMessage);

    if (isGameOver()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
    }
  }

  protected abstract String performTurn();
  protected abstract boolean isGameOver();
  protected abstract void onGameFinish();
  protected abstract Player getNextPlayer();

  public GameBoard getGameBoard() {
    return gameBoard;
  }
}