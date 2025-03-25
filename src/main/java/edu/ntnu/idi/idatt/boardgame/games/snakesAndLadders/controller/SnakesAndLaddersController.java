package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.controller;

import edu.ntnu.idi.idatt.boardgame.common.action.Action;
import edu.ntnu.idi.idatt.boardgame.common.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board.SnakesAndLaddersBoard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.paint.Color;

public class SnakesAndLaddersController extends GameController {
  private final int numberOfPlayers;
  private final List<Color> playerColors =
      List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);

  public SnakesAndLaddersController(int numberOfPlayers) {
    super(new SnakesAndLaddersBoard(), new Dice(2));
    this.numberOfPlayers = numberOfPlayers;
    initialize(numberOfPlayers);
  }

  @Override
  protected Map<Integer, Player> createPlayers(int numberOfPlayers) {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              Player player = new Player(playerId, playerColors.get(playerId - 1));
              players.put(playerId, player);
            });
    return players;
  }

  @Override
  protected String performTurn() {
    Action roll = new RollAction(gameBoard, currentPlayer, dice);
    return roll.execute();
  }

  @Override
  protected boolean isGameOver() {
    return currentPlayer.getPosition() == gameBoard.getBoardSize();
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer.getId());
  }

  @Override
  protected Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }
}
