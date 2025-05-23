package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the game board for Cluedo. It defines the layout of rooms, corridors, and borders, as
 * well as player starting positions.
 */
public final class CluedoBoard implements GameBoard<GridPos> {

  private static final Logger logger = LoggerFactory.getLogger(CluedoBoard.class);
  private static final int BOARD_SIZE = 25;

  // Player starting positions
  private static final GridPos START_POS_MISS_SCARLETT = new GridPos(23, 7); // WHITE
  private static final GridPos START_POS_COL_MUSTARD = new GridPos(17, 1); // RED
  private static final GridPos START_POS_MRS_WHITE = new GridPos(1, 7); // YELLOW
  private static final GridPos START_POS_REV_GREEN = new GridPos(1, 16); // GREEN
  private static final GridPos START_POS_MRS_PEACOCK = new GridPos(6, 23); // BLUE
  private static final GridPos START_POS_PROF_PLUM = new GridPos(19, 23); // PURPLE

  /**
   * The 2D array representing the grid of tiles on the board.
   */
  private final AbstractCluedoTile[][] board = new AbstractCluedoTile[BOARD_SIZE][BOARD_SIZE];

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

  private void populateRoomTiles(RoomDimensions roomDimensions, RoomTile room) {
    for (int row = roomDimensions.top; row <= roomDimensions.bottom; row++) {
      for (int col = roomDimensions.left; col <= roomDimensions.right; col++) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
          if (board[row][col] != null) {
            logger.warn(
                "Warning: Overwriting existing tile at ({}, {}) while populating room {}."
                    + " Existing: {}",
                row,
                col,
                room.getRoomName(),
                board[row][col].getIdentifier());
          }
          board[row][col] = room;
          room.setWalkable(false);
        } else {
          logger.warn(
              "Warning: Attempted to populate room tile outside board bounds at ({}, {})",
              row,
              col);
        }
      }
    }
  }

  private void initializeTiles() {
    insertBorderTiles();
    insertRooms();
    insertCorridorTiles();
    placeStartPosAdjacentBorders();
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

  private void insertRooms() {
    ROOM_SPECS.forEach(
        spec -> {
          // Validate room dimensions against border
          if (spec.dims.top < 1
              || spec.dims.left < 1
              || spec.dims.bottom >= BOARD_SIZE - 1
              || spec.dims.right >= BOARD_SIZE - 1) {
            logger.error(
                "CRITICAL Error: Room {} with dimensions {} extends into the border"
                    + " area or outside bounds.",
                spec.name,
                spec.dims);
            return;
          }
          List<RoomTile.Point> outline = createRectangularOutline(spec.dims);
          RoomTile room = new RoomTile(spec.name, outline);
          populateRoomTiles(spec.dims, room);

          spec.doors.forEach(doorDef -> {
            try {
              room.addDoor(doorDef.roomSide(), doorDef.corridorSide());
            } catch (IllegalArgumentException e) {
              logger.error(
                  "Error adding door for room {} between {} and {}: {}",
                  spec.name,
                  doorDef.roomSide(),
                  doorDef.corridorSide(),
                  e.getMessage());
            }
          });
        });
  }

  private void placeStartPosAdjacentBorders() {
    // PlayerColor.WHITE (Miss Scarlett) -> START_POS_MISS_SCARLETT = new GridPos(23, 7);
    // Tiles adjacent to Miss Scarlett's start
    replaceWithBorderIfCorridor(
        START_POS_MISS_SCARLETT.row(), START_POS_MISS_SCARLETT.col() - 1); // 23, 6
    replaceWithBorderIfCorridor(
        START_POS_MISS_SCARLETT.row(), START_POS_MISS_SCARLETT.col() + 1); // 23, 8

    // PlayerColor.RED (Col. Mustard) -> START_POS_COL_MUSTARD = new GridPos(17, 1);
    // Tiles adjacent to Col. Mustard's start
    replaceWithBorderIfCorridor(
        START_POS_COL_MUSTARD.row() - 1, START_POS_COL_MUSTARD.col()); // 16, 1
    replaceWithBorderIfCorridor(
        START_POS_COL_MUSTARD.row() + 1, START_POS_COL_MUSTARD.col()); // 18, 1

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
    replaceWithBorderIfCorridor(
        START_POS_MRS_PEACOCK.row() - 1, START_POS_MRS_PEACOCK.col()); // 5, 23
    replaceWithBorderIfCorridor(
        START_POS_MRS_PEACOCK.row() + 1, START_POS_MRS_PEACOCK.col()); // 7, 23

    // PlayerColor.PURPLE (Prof. Plum) -> START_POS_PROF_PLUM = new GridPos(19, 23);
    // Tiles adjacent to Prof. Plum's start
    replaceWithBorderIfCorridor(START_POS_PROF_PLUM.row() - 1, START_POS_PROF_PLUM.col()); // 18, 23
    replaceWithBorderIfCorridor(START_POS_PROF_PLUM.row() + 1, START_POS_PROF_PLUM.col()); // 20, 23
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

  private void replaceWithBorderIfCorridor(int row, int col) {
    if (!isValidPosition(new GridPos(row, col))) {
      return;
    }
    AbstractCluedoTile tile = board[row][col];
    if (tile instanceof CorridorTile) {
      board[row][col] = new BorderTile(row, col);
    }
  }

  @Override
  public void setPlayerPosition(Player<GridPos> player, GridPos position) {
    if (!isValidPosition(position)) {
      logger.error("Error: Attempt to set invalid position {}", position);
      return;
    }
    AbstractCluedoTile targetTile = getTileAtPosition(position);
    if (targetTile
        == null) { // Should not happen if isValidPosition is true and board is fully initialized
      logger.error("CRITICAL Error: Target tile is null at valid position {}", position);
      return;
    }

    if (targetTile instanceof BorderTile) {
      logger.error("Error: Cannot set player position to a BorderTile at {}", position);
      return;
    }
    if (!targetTile.isWalkable()
        && !(targetTile
        instanceof RoomTile)) { // Allow moving into rooms even if "not walkable" squares
      logger.error(
          "Error: Cannot set player position to a non-walkable tile at {} of type {}",
          position,
          targetTile.getClass().getSimpleName());
      return;
    }

    GridPos oldPos = player.getPosition();

    if (isValidPosition(oldPos)) {
      AbstractCluedoTile oldTile = getTileAtPosition(oldPos);
      if (oldTile != null) {
        oldTile.removePlayer(player);
      } else {
        logger.warn("Warning: Player {} had no valid old tile at {}", player.getName(), oldPos);
      }
    }

    player.setPosition(position);
    AbstractCluedoTile newTile =
        getTileAtPosition(position); // Re-fetch, could be same as targetTile
    if (newTile != null) {
      newTile.addPlayer(player);
    } else {
      logger.error(
          "CRITICAL Error: New tile is null at valid position {} for player {}",
          position,
          player.getName());
    }
  }

  @Override
  public int getBoardSize() {
    return BOARD_SIZE;
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
                  logger.error(
                      "CRITICAL Error: Calculated start position {} for player {} is a "
                          + "BorderTile. Adjust start positions.",
                      startPos,
                      player.getName());
                  return;
                }
                if (startTile == null) {
                  logger.error(
                      "CRITICAL Error: Start tile is null at supposedly valid position {} "
                          + "for player {}. This might be due to board initialization order "
                          + "or incorrect coordinates.",
                      startPos,
                      player.getName());
                  return;
                }

                player.setPosition(startPos);

                if (!startTile.isWalkable()
                    && !(startTile instanceof RoomTile)) { // Rooms are fine to start in.
                  logger.error(
                      "Error: Start tile at {} for player {} is not walkable and not "
                          + "a Room! Type: {}",
                      startPos,
                      player.getName(),
                      startTile.getClass().getSimpleName());
                }
                startTile.addPlayer(player); // Add player to the tile model

              } else {
                logger.error(
                    "Error: Invalid start position {} calculated for player {}",
                    startPos,
                    player.getName());
              }
            });
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
        logger.warn("Warning: Unmapped player color for start position: {}", color);
        throw new IllegalArgumentException(
            "Invalid or unmapped player color for start position: " + color);
      }
    };
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

  /**
   * Inner record for defining doors between a room and a corridor.
   *
   * @param roomSide     The {@link RoomTile.Point} on the room's side of the door.
   * @param corridorSide The {@link RoomTile.Point} on the corridor's side of the door.
   */
  private record DoorDefinition(RoomTile.Point roomSide, RoomTile.Point corridorSide) {}
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
