package edu.idi.idatt.model;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/** Represents the game board. */
public class GameBoard extends Pane {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;
  private static final int tileSize = 60;
  private static final int gapSize = 5;

  private final Tile[][] tiles = new Tile[ROWS][COLS];

  private final Map<Player, Node> playerIcons = new HashMap<>();

  public GameBoard() {
    GridPane grid = new GridPane();
    grid.setHgap(gapSize);
    grid.setVgap(gapSize);

    buildTiles(grid);
    getChildren().add(grid);

    // Todo: Add snakes and ladders to the board
  }

  /**
   * Builds the tiles of the game board.
   *
   * @param grid the grid to add the tiles to
   */
  private void buildTiles(GridPane grid) {
    IntStream.rangeClosed(1, BOARD_SIZE)
        .forEach(
            pos -> {
              int[] gridPos = getGridCoordinates(pos);
              int col = gridPos[0];
              int row = gridPos[1];
              Tile tile = new Tile(pos, tileSize);
              tiles[row][col] = tile;
              grid.add(tile.getTile(), col, row);
            });
  }

  /**
   * Computes grid coordinates (column, row) for board position p.
   *
   * @param position the board square (1–{@code BOARD_SIZE})
   * @return an int array where index 0 is column and index 1 is row
   */
  private int[] getGridCoordinates(int position) {
    int index = position - 1;
    int rowFromBottom = index / COLS;
    int col;
    if (rowFromBottom % 2 == 0) {
      col = index % COLS;
    } else {
      col = COLS - 1 - index % COLS;
    }
    int gridRow = ROWS - 1 - rowFromBottom;
    return new int[] {col, gridRow};
  }

  public static int getBoardSize() {
    return BOARD_SIZE;
  }

  /**
   * Adds a player to the game board.
   *
   * @param player the player to add
   */
  public void addPlayer(Player player) {
    this.getChildren().add(player.getIcon());
    updatePlayerPosition(player);
  }

  /**
   * Updates the position of a player on the game board.
   *
   * @param player the player to update
   */
  public void updatePlayerPosition(Player player) {
    Node icon = playerIcons.get(player);
    if (icon == null) return;

    int position = player.getPosition();
    if (position < 1 || position > BOARD_SIZE) {
      icon.setVisible(false);
      return;
    }

    Point2D coords = getTileCenter(position);
    if (coords == null) {
      icon.setVisible(false);
      return;
    }

    icon.setLayoutX(coords.getX() - ((Circle) icon).getRadius());
    icon.setLayoutY(coords.getY() - ((Circle) icon).getRadius());
    icon.setVisible(true);
  }

  /**
   * Computes the center of a tile on the game board.
   *
   * @param position the position of the tile
   * @return the center of the tile
   */
  private Point2D getTileCenter(int position) {
    if (position < 1 || position > BOARD_SIZE) return null;

    int[] gridCoords = getGridCoordinates(position);
    int col = gridCoords[0];
    int row = gridCoords[1];

    double x = col * (tileSize + gapSize) + tileSize / 2.0;
    double y = row * (tileSize + gapSize) + tileSize / 2.0;

    return new Point2D(x, y);
  }
}
