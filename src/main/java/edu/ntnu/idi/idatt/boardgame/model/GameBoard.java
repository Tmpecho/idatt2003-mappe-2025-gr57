package edu.ntnu.idi.idatt.boardgame.model;

import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class GameBoard extends Pane {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;
  private static final int TILE_SIZE = 60;
  private static final int GAP_SIZE = 5;

  private final Tile[][] tiles = new Tile[ROWS][COLS];

  public GameBoard() {
    GridPane grid = new GridPane();
    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);

    buildTiles(grid);
    getChildren().add(grid);

    // Todo: Add snakes and ladders to the board
  }

  /**
   * Adds all players to the starting tile (position 1).
   *
   * @param players a map of players keyed by their ID
   */
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
   * Increments a player's position by the given increment. The method removes the player from the
   * old tile and adds them to the new tile.
   *
   * @param player the player to move
   * @param increment the number of positions to move forward
   */
  public void incrementPlayerPosition(Player player, int increment) {
    int oldPos = player.getPosition();
    int newPos = player.incrementPosition(increment);
    getTileAtPosition(oldPos).removePlayer(player);
    getTileAtPosition(newPos).addPlayer(player);
  }

  /**
   * Builds the board tiles and adds them to the grid.
   *
   * @param grid the GridPane to which the tiles are added
   */
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
   * Computes grid coordinates for a given board position. The layout is such that:
   *
   * <ul>
   *   <li>Tile 1 is at the bottom left (col = 0, row = ROWS-1).
   *   <li>Tile BOARD_SIZE is at the top left (col = 0, row = 0).
   *   <li>The board uses a zigzag pattern: even rows (from the bottom) progress left to right, odd
   *       rows right to left.
   * </ul>
   *
   * @param position the board position (1 ≤ position ≤ BOARD_SIZE)
   * @return an array where index 0 is the column and index 1 is the row
   */
  private int[] getGridCoordinates(int position) {
    int index = position - 1;
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
   * @param position the board position (1 ≤ position ≤ BOARD_SIZE)
   * @return the Tile at that position
   */
  private Tile getTileAtPosition(int position) {
    int[] coords = getGridCoordinates(position);
    return tiles[coords[1]][coords[0]];
  }

  public static int getBoardSize() {
    return BOARD_SIZE;
  }
}
