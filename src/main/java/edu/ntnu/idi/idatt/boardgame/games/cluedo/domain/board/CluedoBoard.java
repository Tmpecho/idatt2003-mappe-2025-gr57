package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;

public final class CluedoBoard implements GameBoard<GridPos> {

  private static final int BOARD_SIZE = 25;

  private static final GridPos PLAYER_WHITE_START_POSITION = new GridPos(0, 0);
  private static final GridPos PLAYER_RED_START_POSITION = new GridPos(0, 1);
  private static final GridPos PLAYER_BLUE_START_POSITION = new GridPos(0, 2);
  private static final GridPos PLAYER_GREEN_START_POSITION = new GridPos(0, 3);
  private static final GridPos PLAYER_YELLOW_START_POSITION = new GridPos(0, 4);
  private static final GridPos PLAYER_PURPLE_START_POSITION = new GridPos(0, 5);

  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  public CluedoBoard() {
    initializeTiles();
  }

  private void initializeTiles() {
    // Basic implementation to avoid null tiles
    insertCorridorTiles();
    insertRooms();
  }

  private void insertRooms() {
    // TODO: Implement room placement logic
  }

  private void insertCorridorTiles() {
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (board[row][col] == null) {
          board[row][col] = new CorridorTile(row, col);
        }
      }
    }
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  @Override
  public void setPlayerPosition(Player<GridPos> player, GridPos position) {
    GridPos oldPos = player.getPosition();
    movePlayer(player, oldPos, position);
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player<GridPos>> players) {
    players.values().forEach(player -> {
      GridPos startPos = getPlayerStartPosition(player);
      player.setPosition(startPos);
      AbstractCluedoTile startTile = getTileAtPosition(startPos);
      if (startTile != null) {
        startTile.addPlayer(player);
      }
    });
  }

  @Override
  public void incrementPlayerPosition(Player<GridPos> player, int increment) {
    // For Cluedo, movement is typically grid-based, not incremental like SnL
    // Placeholder: move player by 'increment' steps right (simplified)
    GridPos current = player.getPosition();
    GridPos newPos = new GridPos(current.row(), Math.min(current.col() + increment, BOARD_SIZE - 1));
    setPlayerPosition(player, newPos);
  }

  private AbstractCluedoTile getTileAtPosition(GridPos pos) {
    if (pos.row() >= 0 && pos.row() < BOARD_SIZE && pos.col() >= 0 && pos.col() < BOARD_SIZE) {
      return board[pos.row()][pos.col()];
    }
    return null; // Out of bounds
  }

  private void movePlayer(Player<GridPos> player, GridPos fromPos, GridPos toPos) {
    if (!isAdjacent(fromPos, toPos)) {
      return; // Only allow adjacent moves
    }
    AbstractCluedoTile fromTile = getTileAtPosition(fromPos);
    if (fromTile != null) {
      fromTile.removePlayer(player);
    }
    player.setPosition(toPos);
    AbstractCluedoTile toTile = getTileAtPosition(toPos);
    if (toTile != null) {
      toTile.addPlayer(player);
    }
  }

  private boolean isAdjacent(GridPos from, GridPos to) {
    int rowDiff = Math.abs(from.row() - to.row());
    int colDiff = Math.abs(from.col() - to.col());
    return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
  }

  private GridPos getPlayerStartPosition(Player<GridPos> player) {
    PlayerColor color = player.getColor();
    return switch (color) {
      case WHITE -> PLAYER_WHITE_START_POSITION;
      case RED -> PLAYER_RED_START_POSITION;
      case BLUE -> PLAYER_BLUE_START_POSITION;
      case GREEN -> PLAYER_GREEN_START_POSITION;
      case YELLOW -> PLAYER_YELLOW_START_POSITION;
      case PURPLE -> PLAYER_PURPLE_START_POSITION;
      default -> throw new IllegalArgumentException("Invalid player color: " + color);
    };
  }
}