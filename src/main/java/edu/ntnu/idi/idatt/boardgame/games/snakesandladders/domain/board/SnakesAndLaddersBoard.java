package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;

public class SnakesAndLaddersBoard implements GameBoard {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;

  private final Map<Integer, SnakesAndLaddersTile> tiles = new HashMap<>();
  private final Map<Integer, Connector> connectors = new HashMap<>();

  private static final Map<Integer, Integer> SNAKES = Map.of(
      30, 14,
      34, 7,
      47, 7,
      54, 35,
      65, 5,
      87, 31);

  private static final Map<Integer, Integer> LADDERS = Map.of(
      8, 6,
      21, 10,
      33, 5,
      48, 7,
      61, 8,
      70, 9,
      81, 2);

  public SnakesAndLaddersBoard() {
    initializeTiles();
    addSnakesAndLadders();
  }

  private void initializeTiles() {
    IntStream.rangeClosed(1, BOARD_SIZE)
        .forEach(pos -> tiles.put(pos, new SnakesAndLaddersTile(pos)));
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player> players) {
    players
        .values()
        .forEach(
            player -> {
              player.setPosition(1);
              SnakesAndLaddersTile startTile = getTileAtPosition(1);
              if (startTile != null) {
                startTile.addPlayer(player);
              }
            });
  }

  @Override
  public void incrementPlayerPosition(Player player, int increment) {
    int oldPos = player.getPosition();
    int newPos = oldPos + increment;
    if (newPos > getBoardSize()) {
      newPos = getBoardSize() - (newPos - getBoardSize());
    }
    if (newPos < 1)
      newPos = 1;
    movePlayer(player, oldPos, newPos);
    applyConnectorIfPresent(player);
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  private void movePlayer(Player player, int fromPos, int toPos) {
    SnakesAndLaddersTile fromTile = getTileAtPosition(fromPos);
    if (fromTile != null) {
      fromTile.removePlayer(player);
    }
    player.setPosition(toPos);
    SnakesAndLaddersTile toTile = getTileAtPosition(toPos);
    if (toTile != null) {
      toTile.addPlayer(player);
    }
  }

  private void applyConnectorIfPresent(Player player) {
    int pos = player.getPosition();
    if (!connectors.containsKey(pos)) {
      return;
    }
    Connector connector = connectors.get(pos);
    int destination = connector.getEnd();
    if (destination < 1)
      destination = 1;
    if (destination > getBoardSize())
      destination = getBoardSize();
    movePlayer(player, pos, destination);
  }

  // Define getTileAtPosition only once
  private SnakesAndLaddersTile getTileAtPosition(int pos) {
    return tiles.get(pos);
  }

  // Define addSnakesAndLadders only once
  private void addSnakesAndLadders() {
    SNAKES.forEach(
        (start, length) -> {
          Connector snake = new Snake(start, length);
          connectors.put(start, snake);
        });

    LADDERS.forEach(
        (start, length) -> {
          Connector ladder = new Ladder(start, length);
          connectors.put(start, ladder);
        });
  }

  public Map<Integer, SnakesAndLaddersTile> getTiles() {
    return Collections.unmodifiableMap(tiles);
  }

  public List<Connector> getConnectors() {
    return connectors.values().stream().toList();
  }

  public int getRows() {
    return ROWS;
  }

  public int getCols() {
    return COLS;
  }

  @Override
  public void setPlayerPosition(Player player, int position) {
    int oldPos = player.getPosition();
    movePlayer(player, oldPos, position);
  }
}
