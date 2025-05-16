package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CluedoBoardTest {

    private CluedoBoard board;
    private Map<Integer, Player<GridPos>> players;
    private Player<GridPos> missScarlett; // WHITE
    private Player<GridPos> colMustard;   // RED

    @BeforeEach
    void setUp() {
        board = new CluedoBoard();
        players = new HashMap<>();
        // Dummy start positions
        missScarlett = new Player<>(1, "Miss Scarlett", PlayerColor.WHITE, new GridPos(0,0));
        colMustard = new Player<>(2, "Col. Mustard", PlayerColor.RED, new GridPos(0,0));

        players.put(missScarlett.getId(), missScarlett);
        players.put(colMustard.getId(), colMustard);
    }

    @Test
    void constructor_initializesBoardCorrectly() {
        assertEquals(25, board.getBoardSize());
        assertEquals(25, board.getRows());
        assertEquals(25, board.getCols());

        assertInstanceOf(BorderTile.class, board.getTileAtPosition(new GridPos(0, 0))); // Corner border
        assertInstanceOf(RoomTile.class, board.getTileAtPosition(new GridPos(1, 1))); // Kitchen part
        assertEquals("Kitchen", ((RoomTile)board.getTileAtPosition(new GridPos(1,1))).getRoomName());

        assertInstanceOf(CorridorTile.class, board.getTileAtPosition(new GridPos(7, 7)));

        RoomTile kitchen = (RoomTile) board.getTileAtPosition(new GridPos(1,1));
        assertTrue(kitchen.canEnterFrom(7,4), "Kitchen should have a door at (7,4)");

        // Check a Cluedo center tile (non-walkable room part)
        AbstractCluedoTile cluedoCenterTile = board.getTileAtPosition(new GridPos(10,10));
        assertInstanceOf(RoomTile.class, cluedoCenterTile);
        assertEquals("Cluedo", ((RoomTile)cluedoCenterTile).getRoomName());
        assertFalse(cluedoCenterTile.isWalkable());

        // Check a non-walkable corridor tile near Cluedo center
        assertInstanceOf(CorridorTile.class, board.getTileAtPosition(new GridPos(10, 9)));
        assertFalse(board.getTileAtPosition(new GridPos(10,9)).isWalkable());

        // Miss Scarlett starts at (23,7). (23,6) should be a border.
        assertInstanceOf(BorderTile.class, board.getTileAtPosition(new GridPos(23, 6)), "Tile at (23,6) next to Miss Scarlett start should be BorderTile");

    }

    @Test
    void addPlayersToStart_placesPlayersAtCorrectStartPositions() {
        board.addPlayersToStart(players);

        GridPos scarlettStart = new GridPos(23, 7);
        GridPos mustardStart = new GridPos(17, 1);

        assertEquals(scarlettStart, missScarlett.getPosition());
        assertTrue(board.getTileAtPosition(scarlettStart).getPlayers().contains(missScarlett));

        assertEquals(mustardStart, colMustard.getPosition());
        assertTrue(board.getTileAtPosition(mustardStart).getPlayers().contains(colMustard));
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

        board.setPlayerPosition(missScarlett, studyEntranceCorridor);
        assertEquals(studyEntranceCorridor, missScarlett.getPosition());

        board.setPlayerPosition(missScarlett, studyRoomTile);
        assertEquals(studyRoomTile, missScarlett.getPosition());
        assertTrue(board.getTileAtPosition(studyRoomTile).getPlayers().contains(missScarlett));
        assertTrue(board.getTileAtPosition(studyRoomTile) instanceof RoomTile);
    }


    @Test
    void movePlayer_corridorToRoom_throughValidDoor() {
        board.addPlayersToStart(players);
        GridPos corridorByKitchenDoor = new GridPos(7, 4);
        GridPos kitchenTileByDoor = new GridPos(6,4);

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
        GridPos kitchenTileInsideWall = new GridPos(6,1);

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
        GridPos kitchenTileByDoor = new GridPos(6,4);
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

    // TODO: Test the eventual incrementPlayer logic when done.
    @Test
    void incrementPlayerPosition_logsWarningAndDoesNotChangePosition() {
        board.addPlayersToStart(players);
        GridPos initialPos = missScarlett.getPosition();
        board.incrementPlayerPosition(missScarlett, 5);
        assertEquals(initialPos, missScarlett.getPosition(),
                "incrementPlayerPosition should not change position in Cluedo");
    }
}
