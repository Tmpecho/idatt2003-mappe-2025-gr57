package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CluedoBoardTest {

  private CluedoBoard board;
  private Map<Integer, Player<GridPos>> players;
  private Player<GridPos> missScarlett; // WHITE
  private Player<GridPos> colMustard; // RED
  private Player<GridPos> mrsWhite; // YELLOW

  @BeforeEach
  void setUp() {
    board = new CluedoBoard();
    players = new HashMap<>();
    missScarlett = new Player<>(1, "Miss Scarlett", PlayerColor.WHITE, new GridPos(0, 0));
    colMustard = new Player<>(2, "Col. Mustard", PlayerColor.RED, new GridPos(0, 0));
    mrsWhite = new Player<>(3, "Mrs. White", PlayerColor.YELLOW, new GridPos(0,0));


    players.put(missScarlett.getId(), missScarlett);
    players.put(colMustard.getId(), colMustard);
    players.put(mrsWhite.getId(), mrsWhite);
  }

  @Test
  void constructor_initializesBoardCorrectly() {
    assertEquals(25, board.getBoardSize());
    assertEquals(25, board.getRows());
    assertEquals(25, board.getCols());

    assertInstanceOf(BorderTile.class, board.getTileAtPosition(new GridPos(0, 0)));
    assertInstanceOf(RoomTile.class, board.getTileAtPosition(new GridPos(1, 1)));
    assertEquals("Kitchen", ((RoomTile) board.getTileAtPosition(new GridPos(1, 1))).getRoomName());

    // Check a standard corridor tile that should be walkable
    GridPos walkableCorridorPos = new GridPos(7, 7);
    AbstractCluedoTile walkableCorridorTile = board.getTileAtPosition(walkableCorridorPos);
    assertInstanceOf(CorridorTile.class, walkableCorridorTile);
    assertTrue(walkableCorridorTile.isWalkable(), "Corridor tile at (7,7) should be walkable.");


    RoomTile kitchen = (RoomTile) board.getTileAtPosition(new GridPos(1, 1));
    assertTrue(kitchen.canEnterFrom(7, 4), "Kitchen should have a door at (7,4)");

    AbstractCluedoTile cluedoCenterTile = board.getTileAtPosition(new GridPos(10, 10));
    assertInstanceOf(RoomTile.class, cluedoCenterTile);
    assertEquals("Cluedo", ((RoomTile) cluedoCenterTile).getRoomName());
    assertFalse(cluedoCenterTile.isWalkable());

    assertInstanceOf(CorridorTile.class, board.getTileAtPosition(new GridPos(10, 9)));
    assertFalse(board.getTileAtPosition(new GridPos(10, 9)).isWalkable());

    assertInstanceOf(
            BorderTile.class,
            board.getTileAtPosition(new GridPos(23, 8)),
            "Tile at (23,8) next to Miss Scarlett start should become BorderTile");
    // (23,6) is initially Lounge (RoomTile).
    assertInstanceOf(
            RoomTile.class,
            board.getTileAtPosition(new GridPos(23, 6)),
            "Tile at (23,6) next to Miss Scarlett start should remain RoomTile (Lounge)");
    assertEquals("Lounge", ((RoomTile)board.getTileAtPosition(new GridPos(23,6))).getRoomName());


    assertInstanceOf(
            BorderTile.class,
            board.getTileAtPosition(new GridPos(1, 6)),
            "Tile at (1,6) next to Mrs. White start should become BorderTile");
    GridPos mrsWhiteAdjRoomPos = new GridPos(1,8);
    AbstractCluedoTile mrsWhiteAdjRoomTile = board.getTileAtPosition(mrsWhiteAdjRoomPos);
    assertInstanceOf(
            RoomTile.class,
            mrsWhiteAdjRoomTile,
            "Tile at (1,8) next to Mrs. White start should remain RoomTile (Ball Room)");
    assertEquals("Ball Room", ((RoomTile)mrsWhiteAdjRoomTile).getRoomName());
  }

  @Test
  void addPlayersToStart_placesPlayersAtCorrectStartPositions() {
    board.addPlayersToStart(players);

    GridPos scarlettStart = new GridPos(23, 7);
    GridPos mustardStart = new GridPos(17, 1);
    GridPos whiteStart = new GridPos(1,7);

    assertEquals(scarlettStart, missScarlett.getPosition());
    assertTrue(board.getTileAtPosition(scarlettStart).getPlayers().contains(missScarlett));
    assertTrue(board.getTileAtPosition(scarlettStart).isWalkable(), "Miss Scarlett start tile should be walkable");


    assertEquals(mustardStart, colMustard.getPosition());
    assertTrue(board.getTileAtPosition(mustardStart).getPlayers().contains(colMustard));
    assertTrue(board.getTileAtPosition(mustardStart).isWalkable(), "Col. Mustard start tile should be walkable");

    assertEquals(whiteStart, mrsWhite.getPosition());
    assertTrue(board.getTileAtPosition(whiteStart).getPlayers().contains(mrsWhite));
    assertTrue(board.getTileAtPosition(whiteStart).isWalkable(), "Mrs. White start tile should be walkable");
  }

  @Test
  void setPlayerPosition_validMoveToCorridor() {
    board.addPlayersToStart(players);
    GridPos targetPos = new GridPos(22, 7);

    AbstractCluedoTile oldTile = board.getTileAtPosition(missScarlett.getPosition());
    board.setPlayerPosition(missScarlett, targetPos);

    assertEquals(targetPos, missScarlett.getPosition());
    assertTrue(board.getTileAtPosition(targetPos).getPlayers().contains(missScarlett));
    assertFalse(oldTile.getPlayers().contains(missScarlett));
  }

  @Test
  void setPlayerPosition_invalidMoveToBorderTile_doesNotMove() {
    board.addPlayersToStart(players);
    GridPos originalPos = missScarlett.getPosition();
    GridPos borderPos = new GridPos(0, 0);

    board.setPlayerPosition(missScarlett, borderPos);

    assertEquals(originalPos, missScarlett.getPosition());
    assertFalse(board.getTileAtPosition(borderPos).getPlayers().contains(missScarlett));
    assertTrue(board.getTileAtPosition(originalPos).getPlayers().contains(missScarlett));
  }

  @Test
  void setPlayerPosition_invalidMoveToNonWalkableNonRoomTile_doesNotMove() {
    board.addPlayersToStart(players);
    GridPos originalPos = missScarlett.getPosition();
    GridPos nonWalkableCorridor = new GridPos(10, 9);
    assertFalse(board.getTileAtPosition(nonWalkableCorridor).isWalkable());
    assertInstanceOf(CorridorTile.class, board.getTileAtPosition(nonWalkableCorridor));

    board.setPlayerPosition(missScarlett, nonWalkableCorridor);

    assertEquals(originalPos, missScarlett.getPosition());
    assertFalse(board.getTileAtPosition(nonWalkableCorridor).getPlayers().contains(missScarlett));
  }

  @Test
  void setPlayerPosition_moveToRoom_isAllowed() {
    board.addPlayersToStart(players);
    GridPos studyEntranceCorridor = new GridPos(20, 18);
    GridPos studyRoomTile = new GridPos(21, 18);

    // Move player to the corridor tile first
    board.setPlayerPosition(missScarlett, studyEntranceCorridor);
    assertEquals(studyEntranceCorridor, missScarlett.getPosition());

    // Now attempt to move into the room tile using setPlayerPosition
    board.setPlayerPosition(missScarlett, studyRoomTile);
    assertEquals(studyRoomTile, missScarlett.getPosition());
    assertTrue(board.getTileAtPosition(studyRoomTile).getPlayers().contains(missScarlett));
    assertInstanceOf(RoomTile.class, board.getTileAtPosition(studyRoomTile));
    assertFalse(board.getTileAtPosition(studyRoomTile).isWalkable(), "Study room tile should be !walkable by board init");
  }

  @Test
  void movePlayer_corridorToRoom_throughValidDoor() {
    board.addPlayersToStart(players);
    GridPos corridorByKitchenDoor = new GridPos(7, 4);
    GridPos kitchenTileByDoor = new GridPos(6, 4);

    board.setPlayerPosition(colMustard, corridorByKitchenDoor);
    AbstractCluedoTile oldTile = board.getTileAtPosition(corridorByKitchenDoor);

    board.movePlayer(colMustard, kitchenTileByDoor);

    assertEquals(kitchenTileByDoor, colMustard.getPosition());
    assertTrue(board.getTileAtPosition(kitchenTileByDoor).getPlayers().contains(colMustard));
    assertFalse(oldTile.getPlayers().contains(colMustard));
  }

  @Test
  void movePlayer_corridorToRoom_throughWall_shouldNotMove() {
    board.addPlayersToStart(players);
    GridPos corridorNextToKitchenWall = new GridPos(7, 1);
    GridPos kitchenTileInsideWall = new GridPos(6, 1);

    board.setPlayerPosition(colMustard, corridorNextToKitchenWall);
    AbstractCluedoTile originalTile = board.getTileAtPosition(corridorNextToKitchenWall);

    board.movePlayer(colMustard, kitchenTileInsideWall);

    assertEquals(corridorNextToKitchenWall, colMustard.getPosition());
    assertTrue(originalTile.getPlayers().contains(colMustard));
    assertFalse(board.getTileAtPosition(kitchenTileInsideWall).getPlayers().contains(colMustard));
  }

  @Test
  void movePlayer_roomToCorridor_throughValidDoor() {
    board.addPlayersToStart(players);
    GridPos kitchenTileByDoor = new GridPos(6, 4);
    GridPos corridorByKitchenDoor = new GridPos(7, 4);

    board.setPlayerPosition(colMustard, kitchenTileByDoor);
    AbstractCluedoTile oldTile = board.getTileAtPosition(kitchenTileByDoor);

    board.movePlayer(colMustard, corridorByKitchenDoor);

    assertEquals(corridorByKitchenDoor, colMustard.getPosition());
    assertTrue(board.getTileAtPosition(corridorByKitchenDoor).getPlayers().contains(colMustard));
    assertFalse(oldTile.getPlayers().contains(colMustard));
  }

  @Test
  void getTileAtPosition_outOfBounds_returnsNull() {
    assertNull(board.getTileAtPosition(new GridPos(-1, 5)));
    assertNull(board.getTileAtPosition(new GridPos(5, -1)));
    assertNull(board.getTileAtPosition(new GridPos(25, 5)));
    assertNull(board.getTileAtPosition(new GridPos(5, 25)));
  }

  // TODO: remove incrementplayerposition with test
  @Test
  void incrementPlayerPosition_logsWarningAndDoesNotChangePosition() {
    board.addPlayersToStart(players);
    GridPos initialPos = missScarlett.getPosition();
    board.incrementPlayerPosition(missScarlett, 5);
    assertEquals(
            initialPos,
            missScarlett.getPosition(),
            "incrementPlayerPosition should not change position in Cluedo");
  }
}
