package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.model.Dice;
import edu.ntnu.idi.idatt.boardgame.model.GameBoard;
import edu.ntnu.idi.idatt.boardgame.model.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.paint.Color;

public class Game {
  private final GameBoard gameBoard;
  private final Dice dice;

  int numberOfPlayers = 2;
  private final List<Color> playerColors =
      List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);

  private final Map<Integer, Player> players;

  private Player currentPlayer;

  public Game() {
    this.dice = new Dice(2);
    this.players = createPlayers();
    this.gameBoard = new GameBoard();

    gameBoard.addPlayersToStart(players);

    this.currentPlayer = players.get(1);
  }

  private Map<Integer, Player> createPlayers() {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              Player player = new Player(playerId, playerColors.get(playerId - 1));
              players.put(playerId, player);
            });
    return players;
  }

  public void start() {
  }

  public GameBoard getGameBoard() {
    return gameBoard;
  }
}
