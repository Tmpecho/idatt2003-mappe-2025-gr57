package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.Map;

public final class CluedoBoard implements GameBoard {

  private static final int BOARD_SIZE = 25;

  private static int PLAYER_WHITE_START_POSITION;
  private static int PLAYER_RED_START_POSITION;
  private static int PLAYER_BLUE_START_POSITION;
  private static int PLAYER_GREEN_START_POSITION;
  private static int PLAYER_YELLOW_START_POSITION;
  private static int PLAYER_PURPLE_START_POSITION;

  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  public CluedoBoard() {
    initializeTiles();
  }

  private void initializeTiles() {
    // First add the rooms
    insertRooms();
    // then add the corridors
    insertCorridorTiles();

    // Placeholder values
    PLAYER_WHITE_START_POSITION = 0;
    PLAYER_RED_START_POSITION = 1;
    PLAYER_BLUE_START_POSITION = 2;
    PLAYER_GREEN_START_POSITION = 3;
    PLAYER_YELLOW_START_POSITION = 4;
    PLAYER_PURPLE_START_POSITION = 5;
  }

  private void insertRooms() {
    // todo
  }

  private void insertCorridorTiles() {
    // todo
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  @Override
  public void setPlayerPosition(Player player, int position) {
    int oldPos = player.getPosition();
    movePlayer(player, oldPos, position);
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player> players) {
    players
        .values()
        .forEach(
            player -> {
              int playerStartPosition = getPlayerStartPosition(player);
              player.setPosition(playerStartPosition);
              AbstractCluedoTile startTile = getTileAtPosition(playerStartPosition);
              if (startTile != null) {
                startTile.addPlayer(player);
              }
            });
  }

  private AbstractCluedoTile getTileAtPosition(int playerPosition) {
    // todo: use real logic
    AbstractCluedoTile tile;
    if (playerPositionInRoom(playerPosition)) {
      tile = (RoomTile) board[playerPosition / BOARD_SIZE][playerPosition % BOARD_SIZE];
      return tile;
    }
    tile = (CorridorTile) board[playerPosition / BOARD_SIZE][playerPosition % BOARD_SIZE];
    return tile;
  }

  private boolean playerPositionInRoom(int playerPosition) {
    return false; // todo: implement logic to check if the player is in a room
  }

  private void movePlayer(Player player, int fromPos, int toPos) {
    if (!isAdjacent(fromPos, toPos)) {
      return;
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

  private boolean isAdjacent(int fromPos, int toPos) {
    int fromRow = fromPos / BOARD_SIZE;
    int fromCol = fromPos % BOARD_SIZE;

    int toRow = toPos / BOARD_SIZE;
    int toCol = toPos % BOARD_SIZE;

    return (Math.abs(fromRow - toRow) == 1 && fromCol == toCol)
        || (Math.abs(fromCol - toCol) == 1 && fromRow == toRow);
  }

  private int getPlayerStartPosition(Player player) {
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
