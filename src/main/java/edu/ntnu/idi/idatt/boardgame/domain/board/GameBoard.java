package edu.ntnu.idi.idatt.boardgame.domain.board;

import edu.ntnu.idi.idatt.boardgame.domain.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class GameBoard extends Pane {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;
  private static final int TILE_SIZE = 60;
  private static final int GAP_SIZE = 5;

  private final Tile[][] tiles = new Tile[ROWS][COLS];
  private final Map<Integer, Connector> connectors = new HashMap<>();
  private final Group connectorGroup = new Group();

  private static final Map<Integer, Integer> SNAKES =
      // top, snake length
      Map.of(
          30, 14,
          34, 7,
          47, 7,
          54, 35,
          65, 5,
          87, 31);

  private static final Map<Integer, Integer> LADDERS =
      // bottom, ladder length
      Map.of(
          8, 6,
          21, 10,
          33, 5,
          48, 7,
          61, 8,
          70, 9,
          81, 2);

  public GameBoard() {
    GridPane grid = new GridPane();
    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);

    buildTiles(grid);
    getChildren().addAll(grid, connectorGroup);

    addSnakesAndLadders();
  }

  /** Adds all players to the starting tile (position 1). */
  public void addPlayersToStart(Map<Integer, Player> players) {
    players
        .values()
        .forEach(
            player -> {
              player.setPosition(1);
              getTileAtPosition(1).addPlayer(player);
            });
  }

  /**
   * Increments a player's position by the given increment and returns a log message. After moving
   * normally, if the landing tile is the trigger for a connector, the effect is applied.
   *
   * @param player the player to move
   * @param increment the dice roll increment
   * @return a log message describing the move
   */
  public String incrementPlayerPosition(Player player, int increment) {
    int oldPos = player.getPosition();
    int newPos = player.incrementPosition(increment);

    movePlayer(player, oldPos, newPos);

    String message = "Player " + player.getId() + " rolled a " + increment + " and moved to tile " + newPos;
    String connectorMessage = applyConnectorIfPresent(player);

    if (!connectorMessage.isEmpty()) {
      message += connectorMessage;
    }
    return message;
  }

  /**
   * Moves a player from one tile to another.
   *
   * @param player the player to move
   * @param fromPos the current tile position
   * @param toPos the destination tile position
   */
  private void movePlayer(Player player, int fromPos, int toPos) {
    getTileAtPosition(fromPos).removePlayer(player);
    player.setPosition(toPos);
    getTileAtPosition(toPos).addPlayer(player);
  }

  /**
   * Checks if the player's current position triggers a connector. If so, moves the player
   * accordingly and returns a message.
   *
   * @param player the player to check
   * @return a string message if a connector is applied, or an empty string
   */
  private String applyConnectorIfPresent(Player player) {
    int pos = player.getPosition();

    if (!connectors.containsKey(pos)) {
      return "";
    }
    Connector connector = connectors.get(pos);
    int destination = connector.getEnd();
    movePlayer(player, pos, destination);

    return additionalMessage(connector, destination);
  }

  private static String additionalMessage(Connector connector, int destination) {
    if (connector.getConnectorType().equals("Ladder")) {
      return " and climbed a ladder to tile " + destination;
    } else if (connector.getColor().equals(Color.RED)) {
      return " and slid down a snake to tile " + destination;
    }
    return "";
  }

  private void buildTiles(GridPane grid) {
    IntStream.rangeClosed(1, BOARD_SIZE)
        .forEach(
            pos -> {
              int[] gridPos = getGridCoordinates(pos);
              int col = gridPos[0];
              int row = gridPos[1];

              Tile tile = new Tile(pos, TILE_SIZE);
              tiles[row][col] = tile;
              grid.add(tile.getTile(), col, row);
            });
  }

  /**
   * Computes grid coordinates for a given board position.
   *
   * @param pos the board position (1 ≤ pos ≤ BOARD_SIZE)
   * @return an array {col, row}
   */
  private int[] getGridCoordinates(int pos) {
    int index = pos - 1;
    int rowFromBottom = index / COLS;
    int col;

    if (rowFromBottom % 2 == 0) {
      col = index % COLS;
    } else {
      col = COLS - 1 - (index % COLS);
    }
    // Because row 0 in the grid is the top, we invert the row order.
    int gridRow = ROWS - 1 - rowFromBottom;
    return new int[] {col, gridRow};
  }

  /**
   * Returns the tile corresponding to a given board position.
   *
   * @param pos the board position (1 ≤ pos ≤ BOARD_SIZE)
   * @return the Tile at that position
   */
  private Tile getTileAtPosition(int pos) {
    int[] coords = getGridCoordinates(pos);
    return tiles[coords[1]][coords[0]];
  }

  /**
   * Computes the center pixel coordinates of a tile.
   *
   * @param pos the board position
   * @return a double array {x, y} representing the center
   */
  private double[] getTileCenter(int pos) {
    int[] coords = getGridCoordinates(pos);
    double x = coords[0] * (TILE_SIZE + GAP_SIZE) + TILE_SIZE / 2.0;
    double y = coords[1] * (TILE_SIZE + GAP_SIZE) + TILE_SIZE / 2.0;
    return new double[] {x, y};
  }

  /** Adds a set of snakes and ladders. */
  private void addSnakesAndLadders() {
    SNAKES.forEach(this::addSnake);
    LADDERS.forEach(this::addLadder);
  }

  /**
   * Adds a snake connector. The given start is the snake's head, so if a player lands here, they
   * slide down by the given length.
   */
  private void addSnake(int start, int length) {
    Connector snake = new Snake(start, length);
    connectors.put(start, snake);
    drawConnector(snake);
  }

  /**
   * Adds a ladder connector. The given start is the ladder's bottom; landing here moves the player
   * upward.
   */
  private void addLadder(int start, int length) {
    Connector ladder = new Ladder(start, length);
    connectors.put(start, ladder);
    drawConnector(ladder);
  }

  /** Draws a straight line representing a connector. */
  private void drawConnector(Connector connector) {
    double[] startCenter = getTileCenter(connector.getStart());
    double[] endCenter = getTileCenter(connector.getEnd());

    Line line = new Line(startCenter[0], startCenter[1], endCenter[0], endCenter[1]);

    line.setStroke(connector.getColor());
    line.setStrokeWidth(3);
    connectorGroup.getChildren().add(line);
  }

  public static int getBoardSize() {
    return BOARD_SIZE;
  }
}
