package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;

public final class CluedoBoard implements GameBoard<GridPos> {

  private static final int BOARD_SIZE = 25; // Example size, adjust as needed

  // Define start positions (adjust coordinates as needed)
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
    // Fill with corridor tiles first
    insertCorridorTiles();
    // Then place rooms, overwriting corridor tiles
    insertRooms();
  }

  // Helper to create a rectangular outline for RoomTile
  private List<RoomTile.Point> createRectangularOutline(int topRow, int leftCol, int bottomRow, int rightCol) {
    List<RoomTile.Point> outline = new ArrayList<>();
    outline.add(new RoomTile.Point(topRow, leftCol));
    outline.add(new RoomTile.Point(topRow, rightCol));
    outline.add(new RoomTile.Point(bottomRow, rightCol));
    outline.add(new RoomTile.Point(bottomRow, leftCol));
    outline.add(new RoomTile.Point(topRow, leftCol)); // Close the loop
    return outline;
  }

  private void insertRooms() {
    // Example Room: Kitchen (Top-Left)
    // Define corners (adjust coordinates based on actual Cluedo board layout)
    int kitchenTop = 0, kitchenLeft = 0, kitchenBottom = 5, kitchenRight = 5;
    List<RoomTile.Point> kitchenOutline = createRectangularOutline(kitchenTop, kitchenLeft, kitchenBottom,
        kitchenRight);
    RoomTile kitchen = new RoomTile("Kitchen", kitchenOutline);
    // Add doors (example)
    // kitchen.addDoor(new RoomTile.Point(kitchenBottom, kitchenRight - 1), new
    // RoomTile.Point(kitchenBottom + 1, kitchenRight - 1)); // Door leading down

    // Place the room tiles on the board
    for (int r = kitchenTop; r <= kitchenBottom; r++) {
      for (int c = kitchenLeft; c <= kitchenRight; c++) {
        // For simplicity, make the entire room area a single RoomTile instance
        // conceptually
        // In a more detailed implementation, you might have specific tiles within the
        // room.
        // Here, we just mark the area. The RoomTile itself holds the room info.
        // We can reuse the same RoomTile object for all squares within the room
        // boundary.
        board[r][c] = kitchen;
        kitchen.setWalkable(false); // Rooms are generally not walkable square by square like corridors
      }
    }
    // Mark door locations as walkable or handle entry/exit logic separately
    // Example: Make the tile outside the door walkable, and the tile inside the
    // door reference the room
    // board[kitchenBottom + 1][kitchenRight - 1] = new CorridorTile(kitchenBottom +
    // 1, kitchenRight - 1); // Tile outside door
    // board[kitchenBottom][kitchenRight - 1] = kitchen; // Tile inside door (part
    // of room)
    // For now, we'll keep it simple: the RoomTile object represents the whole room
    // area.

    // Add other rooms similarly (Ballroom, Conservatory, Billiard Room, Library,
    // Study, Hall, Lounge, Dining Room)
    // Example: Study (Bottom-Left)
    int studyTop = 20, studyLeft = 0, studyBottom = 24, studyRight = 6;
    List<RoomTile.Point> studyOutline = createRectangularOutline(studyTop, studyLeft, studyBottom, studyRight);
    RoomTile study = new RoomTile("Study", studyOutline);
    // study.addDoor(...)
    for (int r = studyTop; r <= studyBottom; r++) {
      for (int c = studyLeft; c <= studyRight; c++) {
        board[r][c] = study;
        study.setWalkable(false);
      }
    }

    // ... Add definitions for all other rooms ...
  }

  private void insertCorridorTiles() {
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (board[row][col] == null) { // Only place corridor if no room is there yet
          board[row][col] = new CorridorTile(row, col);
        }
      }
    }
  }

  @Override
  public int getBoardSize() {
    // Return the dimension (assuming square board for simplicity)
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
      // Ensure start position is valid before placing player
      if (isValidPosition(startPos)) {
        player.setPosition(startPos);
        AbstractCluedoTile startTile = getTileAtPosition(startPos);
        if (startTile != null) {
          startTile.addPlayer(player);
        } else {
          System.err.println("Error: Start tile is null for player " + player.getName() + " at " + startPos);
          // Fallback or error handling: place at a default valid position?
          // For now, just log error.
        }
      } else {
        System.err.println("Error: Invalid start position " + startPos + " for player " + player.getName());
      }
    });
  }

  @Override
  public void incrementPlayerPosition(Player<GridPos> player, int increment) {
    // Cluedo movement is based on dice roll limiting steps, not a fixed increment.
    // This method needs a different implementation for Cluedo, likely involving
    // pathfinding
    // or allowing movement to adjacent walkable tiles up to 'increment' steps.
    // For now, this is a placeholder and won't work correctly for Cluedo logic.
    System.out.println("Warning: incrementPlayerPosition is not suitable for Cluedo movement logic.");
    GridPos current = player.getPosition();
    // Simple placeholder: move right, respecting board bounds and basic walkability
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

  // Basic move validation (adjacent and walkable) - can be expanded
  private void movePlayer(Player<GridPos> player, GridPos fromPos, GridPos toPos) {
    if (!isValidPosition(fromPos) || !isValidPosition(toPos))
      return;

    AbstractCluedoTile toTile = getTileAtPosition(toPos);
    if (toTile == null || !toTile.walkable) { // Cannot move to non-walkable tiles directly
      // Allow entering rooms through doors? Needs door logic implementation.
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
    // Allow orthogonal movement only
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
      case YELLOW -> PLAYER_YELLOW_START_POSITION;// White
      case PURPLE -> PLAYER_PURPLE_START_POSITION;// Plum
      // ORANGE is not a standard Cluedo color, handle default or error
      case ORANGE -> throw new IllegalArgumentException("Invalid player color for Cluedo: " + color);
      // Default case if needed, though enum should cover possibilities
      default -> throw new IllegalArgumentException("Invalid player color: " + color);
    };
  }

  // Method to get the underlying 2D array - useful for the view
  public AbstractCluedoTile[][] getBoardGrid() {
    return board;
  }
}