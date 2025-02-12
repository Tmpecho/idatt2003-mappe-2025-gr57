package edu.ntnu.idi.idatt.boardgame.model;

import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/** Represents the game board for a snakes and ladders game. */
public class GameBoard extends Pane {
  private static final int ROWS = 10;
  private static final int COLS = 9;
  private static final int BOARD_SIZE = ROWS * COLS;
  private static final int TILE_SIZE = 60;
  private static final int GAP_SIZE = 5;

  private final Tile[][] tiles = new Tile[ROWS][COLS];

  /** Constructs a GameBoard with a grid of tiles. */
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
   * Increments the player's position by the given increment. Removes the player from the old tile
   * and adds them to the new tile.
   *
   * @param player the player to move
   * @param increment the number of positions to move forward
   */
  public void incrementPlayerPosition(Player player, int increment) {
    int oldPos = player.getPosition();
    int newPos = player.incrementPosition(increment);

    updatePlayerPosition(player, oldPos, newPos);
  }

  private void updatePlayerPosition(Player player, int oldPos, int newPos) {
    getTileAtPosition(oldPos).removePlayer(player);
    getTileAtPosition(newPos).addPlayer(player);
  }

  /**
   * Builds the tiles and adds them to the provided grid.
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
              Tile tile = new Tile(pos, TILE_SIZE);
              tiles[row][col] = tile;
              grid.add(tile.getTile(), col, row);
            });
  }

  /**
   * Computes grid coordinates (column, row) for a given board position. The first tile (position 1)
   * is placed at tiles[0][0].
   *
   * @param position the board square (1–BOARD_SIZE)
   * @return an int array where index 0 is column and index 1 is row
   */
  private int[] getGridCoordinates(int position) {
    int index = position - 1;
    int row = index / COLS;
    int col = index % COLS;

    if (row % 2 != 0) {
      col = COLS - 1 - col;
    }
    return new int[] {col, row};
  }

  /**
   * Returns the Tile corresponding to a given board position.
   *
   * @param position the board position (1–BOARD_SIZE)
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
