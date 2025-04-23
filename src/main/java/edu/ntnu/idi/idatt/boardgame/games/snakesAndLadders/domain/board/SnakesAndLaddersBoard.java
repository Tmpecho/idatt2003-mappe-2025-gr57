package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board;

import edu.ntnu.idi.idatt.boardgame.common.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.common.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class SnakesAndLaddersBoard extends Pane implements GameBoard {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;
  private static final int TILE_SIZE = 60;
  private static final int GAP_SIZE = 5;

  private final Tile[][] snakesAndLaddersTiles = new SnakesAndLaddersTile[ROWS][COLS];
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

  public SnakesAndLaddersBoard() {
    GridPane grid = new GridPane();
    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);
    buildTiles(grid);
    getChildren().addAll(grid, connectorGroup);
    addSnakesAndLadders();
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player> players) {
    players.values().forEach(player -> {
      player.setPosition(1);
      getTileAtPosition(1).addPlayer(player);
    });
  }

  @Override
  public void incrementPlayerPosition(Player player, int increment) {
    int oldPos = player.getPosition();
    int newPos = oldPos + increment;
    if (newPos > getBoardSize()) {
      newPos = getBoardSize() - (newPos - getBoardSize());
    }
    movePlayer(player, oldPos, newPos);
    applyConnectorIfPresent(player);
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  private void movePlayer(Player player, int fromPos, int toPos) {
    getTileAtPosition(fromPos).removePlayer(player);
    player.setPosition(toPos);
    getTileAtPosition(toPos).addPlayer(player);
  }

  private void applyConnectorIfPresent(Player player) {
    int pos = player.getPosition();
    if (!connectors.containsKey(pos)) {
      return;
    }
    Connector connector = connectors.get(pos);
    int destination = connector.getEnd();
    movePlayer(player, pos, destination);
  }

  private void buildTiles(GridPane grid) {
    IntStream.rangeClosed(1, BOARD_SIZE)
        .forEach(
            pos -> {
              int[] gridPos = getGridCoordinates(pos);
              int col = gridPos[0];
              int row = gridPos[1];

              SnakesAndLaddersTile snakesAndLaddersTile = new SnakesAndLaddersTile(pos, TILE_SIZE);
              snakesAndLaddersTiles[row][col] = snakesAndLaddersTile;
              grid.add(snakesAndLaddersTile.getTile(), col, row);
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
    return snakesAndLaddersTiles[coords[1]][coords[0]];
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

  @Override
  public Node getNode() {
    return this;
  }
}
