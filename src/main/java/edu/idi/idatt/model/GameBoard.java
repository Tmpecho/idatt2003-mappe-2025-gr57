package edu.idi.idatt.model;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
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

  private final Map<Integer, Player> players = new HashMap<>();
  private final int numberOfPlayers;

  private final List<Color> playerColors = List.of(
    Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE
  );

  public GameBoard(int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;

    GridPane grid = new GridPane();
    grid.setHgap(gapSize);
    grid.setVgap(gapSize);

    buildTiles(grid);
    createPlayers();
    addPlayersToStart();
    getChildren().add(grid);

    // Todo: Add snakes and ladders to the board
  }

  private void createPlayers() {
    for (int i = 1; i <= numberOfPlayers; i++) {
      Player player = new Player(i, playerColors.get(i - 1));
      players.put(i, player);
    }
  }

  private void addPlayersToStart() {
    players.values().forEach(player -> {
      player.setPosition(1);
      // todo: Add players to tile 1
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
}
