package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;

import java.util.Map;

public final class CluedoBoard {
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
    insertCorridorTiles();
    insertRoomTiles();

    // Placeholder values
    PLAYER_WHITE_START_POSITION = 0;
    PLAYER_RED_START_POSITION = 1;
    PLAYER_BLUE_START_POSITION = 2;
    PLAYER_GREEN_START_POSITION = 3;
    PLAYER_YELLOW_START_POSITION = 4;
    PLAYER_PURPLE_START_POSITION = 5;
  }

  private void insertRoomTiles() {

  }

  private void insertCorridorTiles() {

  }

  public void addPlayersToStart(Map<Integer, Player> players) {
    // each player color starts in their respective starting tile on the edge of the board
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

  private AbstractCluedoTile getTileAtPosition(int playerStartPosition) {
    return null;
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
