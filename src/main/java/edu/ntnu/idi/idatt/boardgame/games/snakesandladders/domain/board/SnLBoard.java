package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class SnLBoard implements GameBoard<LinearPos> {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;

  private final Map<Integer, SnLTile> tiles = new HashMap<>();
  private final Map<Integer, Connector> connectors = new HashMap<>();

  private static final Map<Integer, Integer> SNAKES =
      Map.of(
          30, 14,
          34, 7,
          47, 7,
          54, 35,
          65, 5,
          87, 31);

  private static final Map<Integer, Integer> LADDERS =
      Map.of(
          8, 6,
          21, 10,
          33, 5,
          48, 7,
          61, 8,
          70, 9,
          81, 2);

  public SnLBoard() {
    initializeTiles();
    addSnakesAndLadders();
  }

  private void initializeTiles() {
    IntStream.rangeClosed(1, BOARD_SIZE).forEach(pos -> tiles.put(pos, new SnLTile(pos)));
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player<LinearPos>> players) {
    players
        .values()
        .forEach(
            p -> {
              p.setPosition(new LinearPos(1));
              tiles.get(1).addPlayer(p);
            });
  }

  @Override
  public void incrementPlayerPosition(Player<LinearPos> player, int inc) {
    int from = player.getPosition().index();
    int to = computeDestination(from + inc);
    move(player, from, to);
    applyConnector(player);
  }

  @Override
  public void setPlayerPosition(Player<LinearPos> player, LinearPos pos) {
    move(player, player.getPosition().index(), pos.index());
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  private int computeDestination(int raw) {
    if (raw > BOARD_SIZE) {
      return BOARD_SIZE - (raw - BOARD_SIZE); // bounce back
    }
    return Math.max(raw, 1);
  }

  private void move(Player<LinearPos> p, int from, int to) {
    tiles.get(from).removePlayer(p);
    p.setPosition(new LinearPos(to));
    tiles.get(to).addPlayer(p);
  }

  private void applyConnector(Player<LinearPos> p) {
    int pos = p.getPosition().index();
    Connector c = connectors.get(pos);
    if (c == null) {
      return;
    }
    move(p, pos, computeDestination(c.getEnd()));
  }

  private SnLTile getTileAtPosition(int pos) {
    return tiles.get(pos);
  }

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

  public Map<Integer, SnLTile> getTiles() {
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
}
