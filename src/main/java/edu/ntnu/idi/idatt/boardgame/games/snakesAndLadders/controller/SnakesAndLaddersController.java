package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller;

import edu.ntnu.idi.idatt.boardgame.common.action.Action;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.common.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board.SnakesAndLaddersBoard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SnakesAndLaddersController extends GameController {
  private final int numberOfPlayers;
  private final List<PlayerColor> playerColors = List.of(
          PlayerColor.RED,
          PlayerColor.BLUE,
          PlayerColor.GREEN,
          PlayerColor.YELLOW,
          PlayerColor.ORANGE,
          PlayerColor.PURPLE
  );

  public SnakesAndLaddersController(int numberOfPlayers) {
    super(new SnakesAndLaddersBoard(), new Dice(2));
    this.numberOfPlayers = numberOfPlayers;
    initialize(numberOfPlayers);
  }

  @Override
  protected Map<Integer, Player> createPlayers(int numberOfPlayers) {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers).forEach(playerId -> {
      PlayerColor color = playerColors.get((playerId - 1) % playerColors.size());
      Player player = new Player(playerId, "Player " + playerId, color);
      players.put(playerId, player);
    });
    return players;
  }

  public void rollDice() {
    Action roll = new RollAction(gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(currentPlayer.getName() + " is now at tile " + currentPlayer.getPosition());
    if (isGameOver()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
    }
  }

  @Override
  protected boolean isGameOver() {
    return currentPlayer.getPosition() == gameBoard.getBoardSize();
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }
}