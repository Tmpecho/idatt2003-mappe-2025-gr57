package edu.ntnu.idi.idatt.boardgame.model;

import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/** Represents the game board. */
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

  public void addPlayersToStart(Map<Integer, Player> players) {
    players.values().forEach(player -> {
      player.setPosition(1);

      Tile startTile = tiles[9][0];
	  startTile.addPlayer(player);
    });
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
              Tile tile = new Tile(pos, TILE_SIZE);
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

  public void incrementPlayerPosition(Player Player, int increment) {
    int oldPos = Player.getPosition();
    int newPos = Player.incrementPosition(increment);
    Player.setPosition(newPos);

    Tile oldTile = tiles[getGridCoordinates(oldPos)[1]][getGridCoordinates(oldPos)[0]];
    Tile newTile = tiles[getGridCoordinates(newPos)[1]][getGridCoordinates(newPos)[0]];

    oldTile.removePlayer(Player);
    newTile.addPlayer(Player);
  }
}
