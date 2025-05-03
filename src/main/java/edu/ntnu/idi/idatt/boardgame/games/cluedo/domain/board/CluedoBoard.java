package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CluedoBoard implements GameBoard<GridPos> {

  private static final int BOARD_SIZE = 25;

  private static final GridPos PLAYER_WHITE_START_POSITION = new GridPos(24, 7); // Miss Scarlett
  private static final GridPos PLAYER_RED_START_POSITION = new GridPos(0, 16); // Col. Mustard
  private static final GridPos PLAYER_BLUE_START_POSITION = new GridPos(18, 24); // Mrs. Peacock
  private static final GridPos PLAYER_GREEN_START_POSITION = new GridPos(9, 0); // Rev. Green
  private static final GridPos PLAYER_YELLOW_START_POSITION = new GridPos(24, 14); // Mrs. White
  private static final GridPos PLAYER_PURPLE_START_POSITION = new GridPos(5, 0); // Prof. Plum

  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  public CluedoBoard() {
    initializeTiles();
  }

  private void initializeTiles() {
    insertRooms();
    insertCorridorTiles();
  }

  private List<RoomTile.Point> createRectangularOutline(RoomDimensions roomDimensions) {
    List<RoomTile.Point> outline = new ArrayList<>();
    outline.add(new RoomTile.Point(roomDimensions.getTop(), roomDimensions.getLeft()));
    outline.add(new RoomTile.Point(roomDimensions.getTop(), roomDimensions.getRight()));
    outline.add(new RoomTile.Point(roomDimensions.getBottom(), roomDimensions.getRight()));
    outline.add(new RoomTile.Point(roomDimensions.getBottom(), roomDimensions.getLeft()));
    outline.add(
        new RoomTile.Point(roomDimensions.getTop(), roomDimensions.getLeft())); // Close the loop
    return outline;
  }

  private void populateRoomTiles(RoomDimensions roomDimensions, RoomTile room) {
    for (int r = roomDimensions.getTop(); r <= roomDimensions.getBottom(); r++) {
      for (int c = roomDimensions.getLeft(); c <= roomDimensions.getRight(); c++) {
        board[r][c] = room;
        room.setWalkable(false);
      }
    }
  }

  private void insertRooms() {
    // Example Room: Kitchen (Top-Left)
    RoomDimensions kitchenDimentions = new RoomDimensions(0, 0, 5, 5);
    List<RoomTile.Point> kitchenOutline = createRectangularOutline(kitchenDimentions);
    RoomTile kitchen = new RoomTile("Kitchen", kitchenOutline);
    // Add doors (example)
    // kitchen.addDoor(new RoomTile.Point(kitchenBottom, kitchenRight - 1), new
    // RoomTile.Point(kitchenBottom + 1, kitchenRight - 1)); // Door leading down
    populateRoomTiles(kitchenDimentions, kitchen);

    RoomDimensions studyDimentions = new RoomDimensions(20, 0, 24, 6);
    List<RoomTile.Point> studyOutline = createRectangularOutline(studyDimentions);
    RoomTile study = new RoomTile("Study", studyOutline);
    populateRoomTiles(studyDimentions, study);
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

  public int getRows() {
    return BOARD_SIZE;
  }

  public int getCols() {
    return BOARD_SIZE;
  }

  @Override
  public void setPlayerPosition(Player<GridPos> player, GridPos position) {
    if (isValidPosition(position)) {
      GridPos oldPos = player.getPosition();
      if (isValidPosition(oldPos)) {
        AbstractCluedoTile oldTile = getTileAtPosition(oldPos);
        if (oldTile != null) {
          oldTile.removePlayer(player);
        }
      }
      player.setPosition(position);
      AbstractCluedoTile newTile = getTileAtPosition(position);
      if (newTile != null) {
        newTile.addPlayer(player);
      }
    }
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player<GridPos>> players) {
    players.values().forEach(player -> {
      GridPos startPos = getPlayerStartPosition(player);
      if (isValidPosition(startPos)) {
        player.setPosition(startPos);
        AbstractCluedoTile startTile = getTileAtPosition(startPos);
        if (startTile != null) {
          startTile.addPlayer(player);
        } else {
          System.err.println("Error: Start tile is null for player " + player.getName() + " at " + startPos);
        }
      } else {
        System.err.println("Error: Invalid start position " + startPos + " for player " + player.getName());
      }
    });
  }

  @Override
  public void incrementPlayerPosition(Player<GridPos> player, int increment) {
    // TODO: Implement proper Cluedo movement logic
    System.out.println("Warning: incrementPlayerPosition is not suitable for Cluedo movement logic.");
    GridPos current = player.getPosition();
    int steps = 0;
    GridPos tempPos = current;
    while (steps < increment) {
      GridPos nextPos = new GridPos(tempPos.row(), tempPos.col() + 1);
      if (isValidPosition(nextPos) && getTileAtPosition(nextPos).walkable) {
        tempPos = nextPos;
        steps++;
      } else {
        break; // Stop if cannot move right
      }
    }
    if (!tempPos.equals(current)) {
      setPlayerPosition(player, tempPos);
    }
  }

  public AbstractCluedoTile getTileAtPosition(GridPos pos) {
    if (isValidPosition(pos)) {
      return board[pos.row()][pos.col()];
    }
    return null; // Out of bounds
  }

  private void movePlayer(Player<GridPos> player, GridPos fromPos, GridPos toPos) {
    if (!isValidPosition(fromPos) || !isValidPosition(toPos))
      return;

    AbstractCluedoTile toTile = getTileAtPosition(toPos);
    if (toTile == null || !toTile.walkable) {
      return;
    }

    if (isAdjacent(fromPos, toPos)) {
      AbstractCluedoTile fromTile = getTileAtPosition(fromPos);
      if (fromTile != null) {
        fromTile.removePlayer(player);
      }
      player.setPosition(toPos);
      toTile.addPlayer(player);
    }
  }

  private boolean isAdjacent(GridPos from, GridPos to) {
    int rowDiff = Math.abs(from.row() - to.row());
    int colDiff = Math.abs(from.col() - to.col());
    return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
  }

  private boolean isValidPosition(GridPos pos) {
    return pos != null && pos.row() >= 0 && pos.row() < BOARD_SIZE && pos.col() >= 0 && pos.col() < BOARD_SIZE;
  }

  private GridPos getPlayerStartPosition(Player<GridPos> player) {
    PlayerColor color = player.getColor();
    return switch (color) {
      case WHITE -> PLAYER_WHITE_START_POSITION; // Scarlett
      case RED -> PLAYER_RED_START_POSITION; // Mustard
      case BLUE -> PLAYER_BLUE_START_POSITION; // Peacock
      case GREEN -> PLAYER_GREEN_START_POSITION; // Green
      case YELLOW -> PLAYER_YELLOW_START_POSITION; // White
      case PURPLE -> PLAYER_PURPLE_START_POSITION; // Plum
      default -> throw new IllegalArgumentException("Invalid player color: " + color);
    };
  }

  public AbstractCluedoTile[][] getBoardGrid() {
    return board;
  }

  private record RoomDimensions(int top, int left, int bottom, int right) {
    public int getTop() {
      return top;
    }

    public int getLeft() {
      return left;
    }

    public int getBottom() {
      return bottom;
    }

    public int getRight() {
      return right;
    }
  }
}