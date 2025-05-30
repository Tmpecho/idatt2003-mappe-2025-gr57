package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Represents the game board for Snakes and Ladders. It defines the grid of tiles, and the placement
 * of snakes and ladders.
 */
public final class SnlBoard implements GameBoard<LinearPos> {

  private static final int ROWS = 10;
  private static final int COLS = 9;

  /** Total number of tiles on the board. */
  private static final int BOARD_SIZE = ROWS * COLS;
  // Standard snake positions: key = start (head), value = length (downwards)
  private static final Map<Integer, Integer> SNAKES =
      Map.of(
          30, 14, // 30 -> 16
          34, 7, // 34 -> 27
          47, 7, // 47 -> 40
          54, 35, // 54 -> 19
          65, 5, // 65 -> 60
          87, 31 // 87 -> 56
          );
  // Standard ladder positions: key = start (bottom), value = length (upwards)
  private static final Map<Integer, Integer> LADDERS =
      Map.of(
          8, 6, // 8  -> 14
          21, 10, // 21 -> 31
          33, 5, // 33 -> 38
          48, 7, // 48 -> 55
          61, 8, // 61 -> 69
          70, 9, // 70 -> 79
          81, 2 // 81 -> 83
          );
  /** Map of tile position number to {@link SnlTile} object. */
  private final Map<Integer, SnlTile> tiles = new HashMap<>();
  /** Map of tile position number (start of connector) to {@link Connector} object. */
  private final Map<Integer, Connector> connectors = new HashMap<>();

  /** Constructs the Snakes and Ladders board, initializing tiles, snakes, and ladders. */
  public SnlBoard() {
    initializeTiles();
    addSnakesAndLadders();
  }

  private void initializeTiles() {
    IntStream.rangeClosed(1, BOARD_SIZE).forEach(pos -> tiles.put(pos, new SnlTile(pos)));
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player<LinearPos>> players) {
    players
        .values()
        .forEach(
            player -> {
              player.setPosition(new LinearPos(1));
              tiles.get(1).addPlayer(player);
            });
  }

  /**
   * Increments the player's position by a given amount (e.g., dice roll). Handles bouncing back if
   * the player overshoots the last tile. After moving, it applies any connector (snake or ladder)
   * at the new position.
   *
   * @param player The player to move.
   * @param increment The number of steps to increment the position by.
   */
  public void incrementPlayerPosition(Player<LinearPos> player, int increment) {
    int from = player.getPosition().index();
    int to = computeDestination(from + increment);
    movePlayer(player, from, to);
    applyConnector(player);
  }

  @Override
  public void setPlayerPosition(Player<LinearPos> player, LinearPos pos) {
    movePlayer(player, player.getPosition().index(), pos.index());
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

  private void movePlayer(Player<LinearPos> player, int from, int to) {
    tiles.get(from).removePlayer(player);
    player.setPosition(new LinearPos(to));
    tiles.get(to).addPlayer(player);
  }

  private void applyConnector(Player<LinearPos> player) {
    int pos = player.getPosition().index();
    Connector c = connectors.get(pos);
    if (c == null) {
      return;
    }
    movePlayer(player, pos, computeDestination(c.getEnd()));
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

  /**
   * Gets an unmodifiable map of all tiles on the board. The key is the 1-based tile position, and
   * the value is the {@link SnlTile}.
   *
   * @return An unmodifiable map of tiles.
   */
  public Map<Integer, SnlTile> getTiles() {
    return Collections.unmodifiableMap(tiles);
  }

  /**
   * Gets an unmodifiable list of all connectors (snakes and ladders) on the board.
   *
   * @return An unmodifiable list of connectors.
   */
  public List<Connector> getConnectors() {
    return connectors.values().stream().toList();
  }

  /**
   * Gets the number of rows on the board.
   *
   * @return The number of rows.
   */
  public int getRows() {
    return ROWS;
  }

  /**
   * Gets the number of columns on the board.
   *
   * @return The number of columns.
   */
  public int getCols() {
    return COLS;
  }
}
