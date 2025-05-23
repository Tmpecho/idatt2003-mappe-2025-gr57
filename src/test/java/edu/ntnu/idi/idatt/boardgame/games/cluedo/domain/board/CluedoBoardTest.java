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
  void getTileAtPosition_outOfBounds_returnsNull() {
    assertNull(board.getTileAtPosition(new GridPos(-1, 5)));
    assertNull(board.getTileAtPosition(new GridPos(5, -1)));
    assertNull(board.getTileAtPosition(new GridPos(25, 5)));
    assertNull(board.getTileAtPosition(new GridPos(5, 25)));
  }

  @Test
  void isLegalDestination_validCorridorMove_returnsTrue() {
    GridPos fromPos = new GridPos(10, 10);
    GridPos toPos = new GridPos(10, 9); // Adjacent corridor

    assertTrue(
        board.isLegalDestination(fromPos, toPos), "Legal move between adjacent corridors failed.");
  }

  @Test
  void isLegalDestination_doorEntry_returnsTrue() {
    GridPos corridorPos = new GridPos(12, 8); // Corridor near a door
    GridPos roomPos = new GridPos(12, 7); // Room tile through a door

    assertTrue(
        board.isLegalDestination(corridorPos, roomPos),
        "Legal move into a room through a door failed.");
  }

  @Test
  void isLegalDestination_doorExit_returnsTrue() {
    GridPos roomPos = new GridPos(12, 7); // Room tile
    GridPos corridorPos = new GridPos(12, 8); // Adjacent corridor tile

    assertTrue(
        board.isLegalDestination(roomPos, corridorPos),
        "Legal move to exit a room through a door failed.");
  }

  @Test
  void isLegalDestination_nonAdjacentTiles_returnsFalse() {
    GridPos fromPos = new GridPos(5, 5);
    GridPos toPos = new GridPos(7, 7); // Non-adjacent tile

    assertFalse(
        board.isLegalDestination(fromPos, toPos),
        "Illegal move to a non-adjacent tile was considered legal.");
  }

  @Test
  void isLegalDestination_invalidTileMove_returnsFalse() {
    GridPos fromPos = new GridPos(10, 10);
    GridPos invalidPos = new GridPos(0, 0); // Border or invalid tile

    assertFalse(
        board.isLegalDestination(fromPos, invalidPos),
        "Illegal move to a border or invalid tile was considered legal.");
  }
}
