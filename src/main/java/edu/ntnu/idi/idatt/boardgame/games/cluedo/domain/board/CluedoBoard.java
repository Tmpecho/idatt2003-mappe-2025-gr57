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

  private static final GridPos PLAYER_WHITE_START_POSITION = new GridPos(0, 9); // Mrs. White (Yellow Token) - Top edge
                                                                                // near Kitchen/Ballroom
  private static final GridPos PLAYER_RED_START_POSITION = new GridPos(17, 0); // Col. Mustard (Red Token) - Left edge
                                                                               // near Dining/Lounge
  private static final GridPos PLAYER_BLUE_START_POSITION = new GridPos(6, 24); // Mrs. Peacock (Blue Token) - Right
                                                                                // edge near Conservatory
  private static final GridPos PLAYER_GREEN_START_POSITION = new GridPos(0, 14); // Rev. Green (Green Token) - Top edge
                                                                                 // near Ballroom/Conservatory
  private static final GridPos PLAYER_YELLOW_START_POSITION = new GridPos(24, 7); // Miss Scarlett (Yellow Token) -
  private static final GridPos PLAYER_PURPLE_START_POSITION = new GridPos(19, 24); // Prof. Plum (Purple Token) - Right
                                                                                   // edge near Study/Library

  private static final GridPos START_POS_MISS_SCARLETT = new GridPos(24, 7); // Red Token (uses PlayerColor.WHITE in
                                                                             // code?) - Check PlayerColor enum mapping
  private static final GridPos START_POS_COL_MUSTARD = new GridPos(17, 0); // Yellow Token (uses PlayerColor.RED in
                                                                           // code?)
  private static final GridPos START_POS_MRS_WHITE = new GridPos(0, 9); // White Token (uses PlayerColor.YELLOW in
                                                                        // code?)
  private static final GridPos START_POS_REV_GREEN = new GridPos(0, 14); // Green Token (uses PlayerColor.GREEN in code)
  private static final GridPos START_POS_MRS_PEACOCK = new GridPos(6, 24); // Blue Token (uses PlayerColor.BLUE in code)
  private static final GridPos START_POS_PROF_PLUM = new GridPos(19, 24); // Purple Token (uses PlayerColor.PURPLE in
  // code)

  private static final List<RoomSpec> ROOM_SPECS =
      List.of(
          new RoomSpec("Kitchen", new RoomDimensions(0, 0, 5, 6), true),
          new RoomSpec("Ball Room", new RoomDimensions(0, 8, 7, 16), false),
          new RoomSpec("Conservatory", new RoomDimensions(0, 18, 4, 24), true),
          new RoomSpec("Dining Room", new RoomDimensions(8, 0, 15, 7), false),
          new RoomSpec("Billiard Room", new RoomDimensions(7, 18, 12, 24), false),
          new RoomSpec("Library", new RoomDimensions(14, 17, 18, 24), false),
          new RoomSpec("Study", new RoomDimensions(21, 17, 24, 24), true),
          new RoomSpec("Hall", new RoomDimensions(18, 9, 24, 15), false),
          new RoomSpec("Lounge", new RoomDimensions(19, 0, 24, 6), true));

  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  public CluedoBoard() {
    initializeTiles();
  }

  private void initializeTiles() {
    insertRooms();
    insertCorridorTiles();
    // TODO: Secret passages and doors are not yet implemented here
  }

  private List<RoomTile.Point> createRectangularOutline(RoomDimensions roomDimensions) {
    List<RoomTile.Point> outline = new ArrayList<>();
    outline.add(new RoomTile.Point(roomDimensions.top, roomDimensions.left));
    outline.add(new RoomTile.Point(roomDimensions.top, roomDimensions.right));
    outline.add(new RoomTile.Point(roomDimensions.bottom, roomDimensions.right));
    outline.add(new RoomTile.Point(roomDimensions.bottom, roomDimensions.left));
    outline.add(new RoomTile.Point(roomDimensions.top, roomDimensions.left)); // Close the loop
    return outline;
  }

  private void populateRoomTiles(RoomDimensions roomDimensions, RoomTile room) {
    for (int r = roomDimensions.top; r <= roomDimensions.bottom; r++) {
      for (int c = roomDimensions.left; c <= roomDimensions.right; c++) {
        // Basic bounds check before assignment
        if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
          board[r][c] = room;
          room.setWalkable(false); // Room interiors are not walkable squares in Cluedo movement
        } else {
          System.err.println("Warning: Attempted to populate room tile outside board bounds at (" + r + ", " + c + ")");
        }
      }
    }
  }

  private void insertRooms() {
    ROOM_SPECS.forEach(
        spec -> {
          List<RoomTile.Point> outline = createRectangularOutline(spec.dims);
          RoomTile room = new RoomTile(spec.name, outline);
          populateRoomTiles(spec.dims, room);
          // TODO: if (spec.hasSecretPassage) addSecretPassage(room);
        });
  }

  private void insertCorridorTiles() {
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (board[row][col] == null) {
	        board[row][col] = new CorridorTile(row, col);
        }
      }
    }
    // Mark center area (cellar/stairs) as non-walkable if desired
    // For example:
    for (int r = 10; r <= 16; r++) { // Approximate bounds
      for (int c = 9; c <= 14; c++) { // Approximate bounds
        if (isValidPosition(new GridPos(r, c)) && board[r][c] instanceof CorridorTile) {
          board[r][c].setWalkable(false); // Mark cellar area tiles non-walkable
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
    if (!isValidPosition(position)) {
      System.err.println("Error: Attempt to set invalid position " + position);
      return;
    }
    GridPos oldPos = player.getPosition();

    if (isValidPosition(oldPos)) {
      AbstractCluedoTile oldTile = getTileAtPosition(oldPos);
      if (oldTile != null) {
        oldTile.removePlayer(player);
      } else {
        System.err.println("Warning: Player " + player.getName() + " had no valid old tile at " + oldPos);
      }
    }

    player.setPosition(position);
    AbstractCluedoTile newTile = getTileAtPosition(position);
    if (newTile != null) {
      newTile.addPlayer(player);
    } else {
      System.err.println("Error: New tile is null at valid position " + position + " for player " + player.getName());
    }
  }

  @Override
  public void addPlayersToStart(Map<Integer, Player<GridPos>> players) {
    players
        .values()
        .forEach(
            player -> {
              GridPos startPos = getPlayerStartPosition(player);
              if (isValidPosition(startPos)) {
                // Don't call setPlayerPosition here, as it tries to remove from the old
                // position (null)
                // Directly set position and add to the start tile
                player.setPosition(startPos);
                AbstractCluedoTile startTile = getTileAtPosition(startPos);
                if (startTile != null) {
                  // Make sure start tile is walkable (should be corridor)
                  if (!startTile.isWalkable()) {
                    System.err.println(
                        "Error: Start tile at " + startPos + " for player " + player.getName() + " is not walkable!");
                  }
                  startTile.addPlayer(player);
                } else {
                  System.err.println(
                      "CRITICAL Error: Start tile is null at supposedly valid position "
                          + startPos + " for player " + player.getName());
                }

              } else {
                System.err.println(
                    "Error: Invalid start position "
                        + startPos
                        + " calculated for player "
                        + player.getName());
              }
            });
  }

  // PLACEHOLDER METHOD
  @Override
  public void incrementPlayerPosition(Player<GridPos> player, int increment) {
    // TODO: Implement movement in cluedo
    // This method is generally unsuitable for Cluedo's free movement.
    // Players roll dice and choose path, not move fixed steps on a linear track.
    System.out.println(
        "Warning: incrementPlayerPosition called. This method is not suitable for standard Cluedo movement.");
    // If you needed a basic placeholder:
    // GridPos current = player.getPosition();
    // GridPos nextPos = new GridPos(current.row(), current.col() + increment); //
    // Simplistic move right
    // if (isValidPosition(nextPos) && getTileAtPosition(nextPos).isWalkable()) {
    // setPlayerPosition(player, nextPos);
    // }
  }

  public AbstractCluedoTile getTileAtPosition(GridPos pos) {
    if (isValidPosition(pos)) {
      return board[pos.row()][pos.col()];
    }
    return null; // Out of bounds
  }

  // Example move function (could be used by game logic after validating path)
  public void movePlayer(Player<GridPos> player, GridPos toPos) {
    if (!isValidPosition(toPos)) {
      System.err.println("Invalid move target: " + toPos);
      return;
    }
    AbstractCluedoTile targetTile = getTileAtPosition(toPos);
    if (targetTile == null || !targetTile.isWalkable()) {
      System.err.println("Cannot move to non-walkable tile: " + toPos);
      return;
    }
    // Basic adjacency check - real Cluedo needs pathfinding up to dice roll
    // GridPos fromPos = player.getPosition();
    // if (!isAdjacent(fromPos, toPos)) {
    // System.err.println("Target tile is not adjacent: " + toPos);
    // return;
    // }

    setPlayerPosition(player, toPos); // Use the existing method to handle tile updates
  }

  private boolean isAdjacent(GridPos from, GridPos to) {
    if (from == null || to == null)
      return false;
    int rowDiff = Math.abs(from.row() - to.row());
    int colDiff = Math.abs(from.col() - to.col());
    return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
  }

  private boolean isValidPosition(GridPos pos) {
    return pos != null
        && pos.row() >= 0
        && pos.row() < BOARD_SIZE
        && pos.col() >= 0
        && pos.col() < BOARD_SIZE;
  }

  private GridPos getPlayerStartPosition(Player<GridPos> player) {
    PlayerColor color = player.getColor();
    return switch (color) {
      case WHITE -> START_POS_MISS_SCARLETT; // Assuming WHITE maps to Scarlett (Red Token)
      case RED -> START_POS_COL_MUSTARD; // Assuming RED maps to Mustard (Yellow Token)
      case BLUE -> START_POS_MRS_PEACOCK; // Assuming BLUE maps to Peacock (Blue Token)
      case GREEN -> START_POS_REV_GREEN; // Assuming GREEN maps to Green (Green Token)
      case YELLOW -> START_POS_MRS_WHITE; // Assuming YELLOW maps to Mrs. White (White Token)
      case PURPLE -> START_POS_PROF_PLUM; // Assuming PURPLE maps to Plum (Purple Token)
      default ->
        throw new IllegalArgumentException("Invalid or unmapped player color: " + color);
    };
  }

  public AbstractCluedoTile[][] getBoardGrid() {
    // Consider returning a defensive copy if the board state should be immutable
    // outside this class
    return board;
  }

  // Helper record for clarity
  private record RoomDimensions(int top, int left, int bottom, int right) {}

  // Helper class for room specifications
  private record RoomSpec(String name, RoomDimensions dims, boolean hasSecretPassage) {}
}