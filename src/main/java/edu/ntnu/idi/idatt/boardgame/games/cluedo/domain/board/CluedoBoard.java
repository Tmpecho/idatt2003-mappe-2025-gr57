package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CluedoBoard implements GameBoard<GridPos> {

  private static final int BOARD_SIZE = 25; // This size INCLUDES the 1-tile thick border

  private static final GridPos START_POS_MISS_SCARLETT = new GridPos(23, 7); // PlayerColor.WHITE (Scarlett)
  private static final GridPos START_POS_COL_MUSTARD = new GridPos(17, 1);   // PlayerColor.RED (Mustard)
  private static final GridPos START_POS_MRS_WHITE = new GridPos(1, 7);     // PlayerColor.YELLOW (Mrs. White)
  private static final GridPos START_POS_REV_GREEN = new GridPos(1, 16);    // PlayerColor.GREEN (Rev. Green)
  private static final GridPos START_POS_MRS_PEACOCK = new GridPos(6, 23);  // PlayerColor.BLUE (Mrs. Peacock)
  private static final GridPos START_POS_PROF_PLUM = new GridPos(19, 23);   // PlayerColor.PURPLE (Prof. Plum)

  private static final List<RoomSpec> ROOM_SPECS =
      List.of(
          new RoomSpec("Kitchen", new RoomDimensions(1, 1, 6, 5), true),
          new RoomSpec("Ball Room", new RoomDimensions(1, 8, 7, 15), false),
          new RoomSpec("Conservatory", new RoomDimensions(1, 18, 4, 23), true),
          new RoomSpec("Dining Room", new RoomDimensions(10, 1, 15, 7), false),
          new RoomSpec("Billiard Room", new RoomDimensions(7, 18, 12, 23), false),
          new RoomSpec("Library", new RoomDimensions(14, 17, 18, 23), false),
          new RoomSpec("Study", new RoomDimensions(21, 17, 23, 23), true),
          new RoomSpec("Hall", new RoomDimensions(18, 9, 23, 14), false),
          new RoomSpec("Lounge", new RoomDimensions(19, 1, 23, 6), true),
          new RoomSpec("Cluedo", new RoomDimensions(10, 11, 14, 13), true));

  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  public CluedoBoard() {
    initializeTiles();
  }

  private void initializeTiles() {
    insertBorderTiles();
    insertRooms();
    insertCorridorTiles();
    placeStartPosAdjacentBorders();
    // TODO: Secret passages and doors are not yet implemented here
  }

  private void insertBorderTiles() {
    for (int i = 0; i < BOARD_SIZE; i++) {
      if (board[0][i] == null) board[0][i] = new BorderTile(0, i);
      if (board[BOARD_SIZE - 1][i] == null) board[BOARD_SIZE - 1][i] = new BorderTile(BOARD_SIZE - 1, i);

      if (i > 0 && i < BOARD_SIZE - 1) {
        if (board[i][0] == null) board[i][0] = new BorderTile(i, 0);
        if (board[i][BOARD_SIZE - 1] == null) board[i][BOARD_SIZE - 1] = new BorderTile(i, BOARD_SIZE - 1);
      }
    }
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
        if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
          if (board[r][c] != null) {
             System.err.println("Warning: Overwriting existing tile at (" + r + ", " + c + ") while populating room " + room.getRoomName()
                                + ". Existing: " + board[r][c].getIdentifier());
          }
          board[r][c] = room;
          room.setWalkable(false);
        } else {
          System.err.println("Warning: Attempted to populate room tile outside board bounds at (" + r + ", " + c + ")");
        }
      }
    }
  }

  private void insertRooms() {
    ROOM_SPECS.forEach(
        spec -> {
          // Validate room dimensions against border
          if (spec.dims.top < 1 || spec.dims.left < 1 || spec.dims.bottom >= BOARD_SIZE -1 || spec.dims.right >= BOARD_SIZE -1) {
              System.err.println("CRITICAL Error: Room " + spec.name + " with dimensions " + spec.dims + " extends into the border area or outside bounds.");
              return;
          }
          List<RoomTile.Point> outline = createRectangularOutline(spec.dims);
          RoomTile room = new RoomTile(spec.name, outline);
          populateRoomTiles(spec.dims, room);
          // TODO: if (spec.hasSecretPassage) addSecretPassage(room);
        });
  }

  private void insertCorridorTiles() {
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (board[row][col] == null) { // If not a border and not a room, it's a corridor
	        board[row][col] = new CorridorTile(row, col);
        }
      }
    }
    // Mark center area (cellar/stairs) as non-walkable if desired
    // These coordinates should be fine as they are well within the board.
    for (int r = 10; r <= 16; r++) { // Approximate bounds
      for (int c = 9; c <= 14; c++) { // Approximate bounds
        if (isValidPosition(new GridPos(r, c)) && board[r][c] instanceof CorridorTile) {
          board[r][c].setWalkable(false); // Mark cellar area tiles non-walkable
        }
      }
    }
  }

  private void placeStartPosAdjacentBorders() {
    // PlayerColor.WHITE (Miss Scarlett) -> START_POS_MISS_SCARLETT = new GridPos(23, 7);
    // Bottom edge: place borders left (23,6) and right (23,8)
    replaceWithBorderIfCorridor(23, 6);
    replaceWithBorderIfCorridor(23, 8);

    // PlayerColor.RED (Col. Mustard) -> START_POS_COL_MUSTARD = new GridPos(17, 1);
    // Left edge: place borders above (16,1) and below (18,1)
    replaceWithBorderIfCorridor(16, 1);
    replaceWithBorderIfCorridor(18, 1);

    // PlayerColor.YELLOW (Mrs. White) -> START_POS_MRS_WHITE = new GridPos(1, 9);
    // Top edge: place borders left (1,8) and right (1,10)
    replaceWithBorderIfCorridor(1, 8);
    replaceWithBorderIfCorridor(1, 10);

    // PlayerColor.GREEN (Rev. Green) -> START_POS_REV_GREEN = new GridPos(1, 14);
    // Top edge: place borders left (1,13) and right (1,15)
    replaceWithBorderIfCorridor(1, 13);
    replaceWithBorderIfCorridor(1, 15);

    // PlayerColor.BLUE (Mrs. Peacock) -> START_POS_MRS_PEACOCK = new GridPos(6, 23);
    // Right edge: place borders above (5,23) and below (7,23)
    replaceWithBorderIfCorridor(5, 23);
    replaceWithBorderIfCorridor(7, 23);

    // PlayerColor.PURPLE (Prof. Plum) -> START_POS_PROF_PLUM = new GridPos(19, 23);
    // Right edge: place borders above (18,23) and below (20,23)
    replaceWithBorderIfCorridor(18, 23);
    replaceWithBorderIfCorridor(20, 23);
  }

  private void replaceWithBorderIfCorridor(int r, int c) {
    if (isValidPosition(new GridPos(r, c))) {
      AbstractCluedoTile tile = board[r][c];
      // Only replace if it's a CorridorTile and not part of a room or already a border.
      // The initial border is at 0 and BOARD_SIZE-1. These replacements are for tiles within the playable area.
      if (tile instanceof CorridorTile) {
        board[r][c] = new BorderTile(r, c);
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
    AbstractCluedoTile targetTile = getTileAtPosition(position);
    if (targetTile instanceof BorderTile) {
        System.err.println("Error: Cannot set player position to a BorderTile at " + position);
        return;
    }
    if (!targetTile.isWalkable() && !(targetTile instanceof RoomTile)) { // Allow moving into rooms even if "not walkable" squares
        System.err.println("Error: Cannot set player position to a non-walkable tile at " + position + " of type " + targetTile.getClass().getSimpleName());
        return;
    }


    GridPos oldPos = player.getPosition();

    if (isValidPosition(oldPos)) {
      AbstractCluedoTile oldTile = getTileAtPosition(oldPos);
      if (oldTile != null) {
        oldTile.removePlayer(player);
      } else {
        // This can happen if player was at an uninitialized/invalid old position
        System.err.println("Warning: Player " + player.getName() + " had no valid old tile at " + oldPos);
      }
    } else if (oldPos != null) {
        // Player might be starting, oldPos could be (0,0) by default from Player constructor if not immediately set
        // System.err.println("Warning: Player " + player.getName() + " had an invalid old position " + oldPos);
    }


    player.setPosition(position);
    AbstractCluedoTile newTile = getTileAtPosition(position); // Re-fetch, could be same as targetTile
    if (newTile != null) {
      newTile.addPlayer(player);
    } else {
      // This should not happen if isValidPosition passed and position is not BorderTile
      System.err.println("CRITICAL Error: New tile is null at valid position " + position + " for player " + player.getName());
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
                AbstractCluedoTile startTile = getTileAtPosition(startPos);
                if (startTile instanceof BorderTile) {
                    System.err.println(
                        "CRITICAL Error: Calculated start position " + startPos + " for player " + player.getName() + " is a BorderTile. Adjust start positions.");
                    return;
                }
                if (startTile == null) {
                     System.err.println(
                      "CRITICAL Error: Start tile is null at supposedly valid position "
                          + startPos + " for player " + player.getName() + ". This might be due to board initialization order or incorrect coordinates.");
                    return;
                }

                player.setPosition(startPos); // Set player's internal position

                if (!startTile.isWalkable() && !(startTile instanceof RoomTile)) { // Rooms are fine to start in.
                  System.err.println(
                      "Error: Start tile at " + startPos + " for player " + player.getName() + " is not walkable and not a Room! Type: " + startTile.getClass().getSimpleName());
                }
                startTile.addPlayer(player); // Add player to the tile model

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
    System.out.println(
        "Warning: incrementPlayerPosition called. This method is not suitable for standard Cluedo movement.");
  }

  public AbstractCluedoTile getTileAtPosition(GridPos pos) {
    if (isValidPosition(pos)) {
      return board[pos.row()][pos.col()];
    }
    return null; // Out of bounds
  }

  public void movePlayer(Player<GridPos> player, GridPos toPos) {
    if (!isValidPosition(toPos)) {
      System.err.println("Invalid move target: " + toPos);
      return;
    }
    AbstractCluedoTile targetTile = getTileAtPosition(toPos);
    if (targetTile == null) {
        System.err.println("Cannot move to null tile: " + toPos);
        return;
    }
    if (targetTile instanceof BorderTile) {
        System.err.println("Cannot move to a BorderTile: " + toPos);
        return;
    }
    if (!targetTile.isWalkable() && !(targetTile instanceof RoomTile)) { // Allow entering rooms
      System.err.println("Cannot move to non-walkable tile: " + toPos + " of type " + targetTile.getClass().getSimpleName());
      return;
    }
    setPlayerPosition(player, toPos);
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
    // Mapping player character names (implied by PlayerColor choice in controller) to their start positions
    PlayerColor color = player.getColor();
    return switch (color) {
      // Note: The PlayerColor enum itself doesn't strictly map to Cluedo characters.
      // The CluedoController assigns colors sequentially.
      // This mapping assumes a common association of colors to characters or relies on controller's assignment.
      case WHITE -> START_POS_MISS_SCARLETT;  // Traditionally Red token
      case RED -> START_POS_COL_MUSTARD;      // Traditionally Yellow token
      case YELLOW -> START_POS_MRS_WHITE;     // Traditionally White token
      case GREEN -> START_POS_REV_GREEN;      // Traditionally Green token
      case BLUE -> START_POS_MRS_PEACOCK;     // Traditionally Blue token
      case PURPLE -> START_POS_PROF_PLUM;     // Traditionally Purple token
      default -> {
          // Fallback for >6 players or unmapped colors, though CluedoController limits to 6.
          // This could be an error or assign to a default non-border tile.
          System.err.println("Warning: Unmapped player color for start position: " + color + ". Defaulting to a generic safe spot if possible, or erroring.");
          // A "safe" default might be (1,1) if it's a corridor, but this indicates a setup issue.
          throw new IllegalArgumentException("Invalid or unmapped player color for start position: " + color);
        }
    };
  }


  public AbstractCluedoTile[][] getBoardGrid() {
    return board;
  }

  private record RoomDimensions(int top, int left, int bottom, int right) {}

  private record RoomSpec(String name, RoomDimensions dims, boolean hasSecretPassage) {}
}