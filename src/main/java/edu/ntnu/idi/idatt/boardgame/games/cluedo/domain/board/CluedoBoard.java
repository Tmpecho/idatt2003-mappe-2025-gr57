package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Represents the game board for Cluedo. It defines the layout of rooms, corridors, and borders, as
 * well as player starting positions.
 */
public final class CluedoBoard implements GameBoard<GridPos> {

  private static final int BOARD_SIZE = 25;

  // Player starting positions
  private static final GridPos START_POS_MISS_SCARLETT = new GridPos(23, 7); // WHITE
  private static final GridPos START_POS_COL_MUSTARD = new GridPos(17, 1); // RED
  private static final GridPos START_POS_MRS_WHITE = new GridPos(1, 7); // YELLOW
  private static final GridPos START_POS_REV_GREEN = new GridPos(1, 16); // GREEN
  private static final GridPos START_POS_MRS_PEACOCK = new GridPos(6, 23); // BLUE
  private static final GridPos START_POS_PROF_PLUM = new GridPos(19, 23); // PURPLE


  /**
   * Inner record for defining doors between a room and a corridor.
   *
   * @param roomSide     The {@link RoomTile.Point} on the room's side of the door.
   * @param corridorSide The {@link RoomTile.Point} on the corridor's side of the door.
   */
  private record DoorDefinition(RoomTile.Point roomSide, RoomTile.Point corridorSide) {

  }

  /**
   * Specifications for each room on the Cluedo board, including dimensions, and door definitions.
   */
  private static final List<RoomSpec> ROOM_SPECS =
      List.of(
          new RoomSpec(
              "Kitchen",
              new RoomDimensions(1, 1, 6, 5),
              true,
              List.of(new DoorDefinition(new RoomTile.Point(6, 4), new RoomTile.Point(7, 4)))),
          new RoomSpec(
              "Ball Room",
              new RoomDimensions(1, 8, 7, 15),
              false,
              List.of(
                  new DoorDefinition(new RoomTile.Point(5, 8), new RoomTile.Point(5, 7)),
                  new DoorDefinition(new RoomTile.Point(7, 9), new RoomTile.Point(8, 9)),
                  new DoorDefinition(new RoomTile.Point(7, 14), new RoomTile.Point(8, 14)),
                  new DoorDefinition(new RoomTile.Point(5, 15), new RoomTile.Point(5, 16)))),
          new RoomSpec(
              "Conservatory",
              new RoomDimensions(1, 18, 4, 23),
              true,
              List.of(new DoorDefinition(new RoomTile.Point(4, 18), new RoomTile.Point(4, 17)))),
          new RoomSpec(
              "Dining Room",
              new RoomDimensions(10, 1, 15, 7),
              false,
              List.of(
                  new DoorDefinition(new RoomTile.Point(12, 7), new RoomTile.Point(12, 8)),
                  new DoorDefinition(new RoomTile.Point(15, 6), new RoomTile.Point(16, 6)))),
          new RoomSpec(
              "Billiard Room",
              new RoomDimensions(7, 18, 12, 23),
              false,
              List.of(
                  new DoorDefinition(new RoomTile.Point(8, 18), new RoomTile.Point(8, 17)),
                  new DoorDefinition(new RoomTile.Point(12, 22), new RoomTile.Point(13, 22)))),
          new RoomSpec(
              "Library",
              new RoomDimensions(14, 17, 18, 23),
              false,
              List.of(
                  new DoorDefinition(new RoomTile.Point(14, 20), new RoomTile.Point(13, 20)),
                  new DoorDefinition(new RoomTile.Point(16, 17), new RoomTile.Point(16, 16)))),
          new RoomSpec(
              "Study",
              new RoomDimensions(21, 17, 23, 23),
              true,
              List.of(new DoorDefinition(new RoomTile.Point(21, 18), new RoomTile.Point(20, 18)))),
          new RoomSpec(
              "Hall",
              new RoomDimensions(18, 9, 23, 14),
              false,
              List.of(
                  new DoorDefinition(new RoomTile.Point(18, 11), new RoomTile.Point(17, 11)),
                  new DoorDefinition(new RoomTile.Point(18, 12), new RoomTile.Point(17, 12)),
                  new DoorDefinition(new RoomTile.Point(20, 9), new RoomTile.Point(20, 8)),
                  new DoorDefinition(new RoomTile.Point(21, 14), new RoomTile.Point(21, 15)))),
          new RoomSpec(
              "Lounge",
              new RoomDimensions(19, 1, 23, 6),
              true,
              List.of(new DoorDefinition(new RoomTile.Point(19, 6), new RoomTile.Point(18, 6)))),
          new RoomSpec("Cluedo", new RoomDimensions(10, 10, 14, 14), true, List.of()));

  /**
   * The 2D array representing the grid of tiles on the board.
   */
  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

  /**
   * Constructs the Cluedo board, initializing all tiles (borders, rooms, corridors).
   */
  public CluedoBoard() {
    initializeTiles();
  }

  /**
   * Checks if a move from one position to another is legal based on the game rules.
   *
   * @param fromPosition   The starting position.
   * @param targetPosition The target position.
   * @return True if the move is legal, false otherwise.
   */
  public boolean isLegalDestination(GridPos fromPosition, GridPos targetPosition) {
    AbstractCluedoTile fromTile = getTileAtPosition(fromPosition);
    AbstractCluedoTile toTile = getTileAtPosition(targetPosition);

    boolean adjacent =
        Math.abs(fromPosition.row() - targetPosition.row())
            + Math.abs(fromPosition.col() - targetPosition.col())
            == 1;

    boolean corridorToCorridor =
        fromTile instanceof CorridorTile && toTile instanceof CorridorTile && adjacent;

    boolean doorEntry =
        fromTile instanceof CorridorTile
            && toTile instanceof RoomTile
            && adjacent // must stand right outside the door
            && ((RoomTile) toTile).canEnterFrom(fromPosition.row(), fromPosition.col());

    boolean doorExit =
        fromTile instanceof RoomTile room
            && toTile instanceof CorridorTile
            && room.canExitTo(targetPosition.row(), targetPosition.col());

    // reject anything but corridor->corridor, corridor->room, room->corridor
    return corridorToCorridor || doorEntry || doorExit;
  }

  private void initializeTiles() {
    insertBorderTiles();
    insertRooms();
    insertCorridorTiles();
    placeStartPosAdjacentBorders();
    // TODO: Secret passages are not yet implemented here
  }

  private void insertBorderTiles() {
    IntStream.range(0, BOARD_SIZE)
        .forEach(
            i -> {
              if (board[0][i] == null) {
                board[0][i] = new BorderTile(0, i);
              }
              if (board[BOARD_SIZE - 1][i] == null) {
                board[BOARD_SIZE - 1][i] = new BorderTile(BOARD_SIZE - 1, i);
              }
              if (i > 0 && i < BOARD_SIZE - 1) {
                if (board[i][0] == null) {
                  board[i][0] = new BorderTile(i, 0);
                }
                if (board[i][BOARD_SIZE - 1] == null) {
                  board[i][BOARD_SIZE - 1] = new BorderTile(i, BOARD_SIZE - 1);
                }
              }
            });
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
            System.err.println(
                "Warning: Overwriting existing tile at ("
                    + r
                    + ", "
                    + c
                    + ") while populating room "
                    + room.getRoomName()
                    + ". Existing: "
                    + board[r][c].getIdentifier());
          }
          board[r][c] = room;
          room.setWalkable(false);
        } else {
          System.err.println(
              "Warning: Attempted to populate room tile outside board bounds at ("
                  + r
                  + ", "
                  + c
                  + ")");
        }
      }
    }
  }

  private void insertRooms() {
    ROOM_SPECS.forEach(
        spec -> {
          // Validate room dimensions against border
          if (spec.dims.top < 1
              || spec.dims.left < 1
              || spec.dims.bottom >= BOARD_SIZE - 1
              || spec.dims.right >= BOARD_SIZE - 1) {
            System.err.println(
                "CRITICAL Error: Room "
                    + spec.name
                    + " with dimensions "
                    + spec.dims
                    + " extends into the border area or outside bounds.");
            return;
          }
          List<RoomTile.Point> outline = createRectangularOutline(spec.dims);
          RoomTile room = new RoomTile(spec.name, outline);
          populateRoomTiles(spec.dims, room);

          for (DoorDefinition doorDef : spec.doors) {
            try {
              room.addDoor(doorDef.roomSide(), doorDef.corridorSide());
            } catch (IllegalArgumentException e) {
              System.err.println(
                  "Error adding door for room "
                      + spec.name
                      + " between "
                      + doorDef.roomSide()
                      + " and "
                      + doorDef.corridorSide()
                      + ": "
                      + e.getMessage());
            }
          }
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
  }

  private void placeStartPosAdjacentBorders() {
    // PlayerColor.WHITE (Miss Scarlett) -> START_POS_MISS_SCARLETT = new GridPos(23, 7);
    // Tiles adjacent to Miss Scarlett's start
    replaceWithBorderIfCorridor(START_POS_MISS_SCARLETT.row(),
        START_POS_MISS_SCARLETT.col() - 1); // 23, 6
    replaceWithBorderIfCorridor(START_POS_MISS_SCARLETT.row(),
        START_POS_MISS_SCARLETT.col() + 1); // 23, 8

    // PlayerColor.RED (Col. Mustard) -> START_POS_COL_MUSTARD = new GridPos(17, 1);
    // Tiles adjacent to Col. Mustard's start
    replaceWithBorderIfCorridor(START_POS_COL_MUSTARD.row() - 1,
        START_POS_COL_MUSTARD.col()); // 16, 1
    replaceWithBorderIfCorridor(START_POS_COL_MUSTARD.row() + 1,
        START_POS_COL_MUSTARD.col()); // 18, 1

    // PlayerColor.YELLOW (Mrs. White) -> START_POS_MRS_WHITE = new GridPos(1, 7);
    // Tiles adjacent to Mrs. White's start
    replaceWithBorderIfCorridor(START_POS_MRS_WHITE.row(), START_POS_MRS_WHITE.col() - 1); // 1, 6
    replaceWithBorderIfCorridor(START_POS_MRS_WHITE.row(), START_POS_MRS_WHITE.col() + 1); // 1, 8

    // PlayerColor.GREEN (Rev. Green) -> START_POS_REV_GREEN = new GridPos(1, 16);
    // Tiles adjacent to Rev. Green's start
    replaceWithBorderIfCorridor(START_POS_REV_GREEN.row(), START_POS_REV_GREEN.col() - 1); // 1, 15
    replaceWithBorderIfCorridor(START_POS_REV_GREEN.row(), START_POS_REV_GREEN.col() + 1); // 1, 17

    // PlayerColor.BLUE (Mrs. Peacock) -> START_POS_MRS_PEACOCK = new GridPos(6, 23);
    // Tiles adjacent to Mrs. Peacock's start
    replaceWithBorderIfCorridor(START_POS_MRS_PEACOCK.row() - 1,
        START_POS_MRS_PEACOCK.col()); // 5, 23
    replaceWithBorderIfCorridor(START_POS_MRS_PEACOCK.row() + 1,
        START_POS_MRS_PEACOCK.col()); // 7, 23

    // PlayerColor.PURPLE (Prof. Plum) -> START_POS_PROF_PLUM = new GridPos(19, 23);
    // Tiles adjacent to Prof. Plum's start
    replaceWithBorderIfCorridor(START_POS_PROF_PLUM.row() - 1, START_POS_PROF_PLUM.col()); // 18, 23
    replaceWithBorderIfCorridor(START_POS_PROF_PLUM.row() + 1, START_POS_PROF_PLUM.col()); // 20, 23
  }

  private void replaceWithBorderIfCorridor(int r, int c) {
    if (!isValidPosition(new GridPos(r, c))) {
      return;
    }
    AbstractCluedoTile tile = board[r][c];
    if (tile instanceof CorridorTile) {
      board[r][c] = new BorderTile(r, c);
    }
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
  }

  /**
   * Gets the number of rows on the board.
   *
   * @return The number of rows.
   */
  public int getRows() {
    return BOARD_SIZE;
  }

  /**
   * Gets the number of columns on the board.
   *
   * @return The number of columns.
   */
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
    if (targetTile
        == null) { // Should not happen if isValidPosition is true and board is fully initialized
      System.err.println("CRITICAL Error: Target tile is null at valid position " + position);
      return;
    }

    if (targetTile instanceof BorderTile) {
      System.err.println("Error: Cannot set player position to a BorderTile at " + position);
      return;
    }
    if (!targetTile.isWalkable()
        && !(targetTile
        instanceof RoomTile)) { // Allow moving into rooms even if "not walkable" squares
      System.err.println(
          "Error: Cannot set player position to a non-walkable tile at "
              + position
              + " of type "
              + targetTile.getClass().getSimpleName());
      return;
    }

    GridPos oldPos = player.getPosition();

    if (isValidPosition(oldPos)) {
      AbstractCluedoTile oldTile = getTileAtPosition(oldPos);
      if (oldTile != null) {
        oldTile.removePlayer(player);
      } else {
        System.err.println(
            "Warning: Player " + player.getName() + " had no valid old tile at " + oldPos);
      }
    }

    player.setPosition(position);
    AbstractCluedoTile newTile =
        getTileAtPosition(position); // Re-fetch, could be same as targetTile
    if (newTile != null) {
      newTile.addPlayer(player);
    } else {
      System.err.println(
          "CRITICAL Error: New tile is null at valid position "
              + position
              + " for player "
              + player.getName());
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
                      "CRITICAL Error: Calculated start position "
                          + startPos
                          + " for player "
                          + player.getName()
                          + " is a BorderTile. Adjust start positions.");
                  return;
                }
                if (startTile == null) {
                  System.err.println(
                      "CRITICAL Error: Start tile is null at supposedly valid position "
                          + startPos
                          + " for player "
                          + player.getName()
                          + ". This might be due to board initialization "
                          + "order or incorrect coordinates.");
                  return;
                }

                player.setPosition(startPos);

                if (!startTile.isWalkable()
                    && !(startTile instanceof RoomTile)) { // Rooms are fine to start in.
                  System.err.println(
                      "Error: Start tile at "
                          + startPos
                          + " for player "
                          + player.getName()
                          + " is not walkable and not a Room! Type: "
                          + startTile.getClass().getSimpleName());
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

  /**
   * Retrieves the {@link AbstractCluedoTile} at the given grid position.
   *
   * @param pos The {@link GridPos} to query.
   * @return The tile at the position, or null if the position is invalid or out of bounds.
   */
  public AbstractCluedoTile getTileAtPosition(GridPos pos) {
    if (isValidPosition(pos)) {
      return board[pos.row()][pos.col()];
    }
    return null;
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
      case WHITE -> START_POS_MISS_SCARLETT;
      case RED -> START_POS_COL_MUSTARD;
      case YELLOW -> START_POS_MRS_WHITE;
      case GREEN -> START_POS_REV_GREEN;
      case BLUE -> START_POS_MRS_PEACOCK;
      case PURPLE -> START_POS_PROF_PLUM;
      default -> {
        System.err.println("Warning: Unmapped player color for start position: " + color);
        throw new IllegalArgumentException(
            "Invalid or unmapped player color for start position: " + color);
      }
    };
  }

  /**
   * Gets the underlying 2D array of {@link AbstractCluedoTile} objects representing the board grid.
   * Primarily for view rendering.
   *
   * @return The 2D array of tiles.
   */
  public AbstractCluedoTile[][] getBoardGrid() {
    return board;
  }

  /**
   * Record to define the rectangular dimensions of a room. Coordinates are inclusive and
   * 0-indexed.
   *
   * @param top    The top-most row index.
   * @param left   The left-most column index.
   * @param bottom The bottom-most row index.
   * @param right  The right-most column index.
   */
  private record RoomDimensions(int top, int left, int bottom, int right) {

  }

  /**
   * Record to encapsulate the specification for a room on the board.
   *
   * @param name             The name of the room.
   * @param dims             The {@link RoomDimensions} of the room.
   * @param hasSecretPassage True if the room has a secret passage (feature not fully implemented).
   * @param doors            A list of {@link DoorDefinition}s for the room.
   */
  private record RoomSpec(
      String name, RoomDimensions dims, boolean hasSecretPassage, List<DoorDefinition> doors) {

  }
}
