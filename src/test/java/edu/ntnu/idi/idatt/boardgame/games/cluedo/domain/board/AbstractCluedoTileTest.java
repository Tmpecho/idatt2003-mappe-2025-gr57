package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AbstractCluedoTileTest {

    private ConcreteTestCluedoTile tile;
    private Player<GridPos> player1;
    private Player<GridPos> player2;
    private TileObserver<GridPos> mockObserver;

    // Concrete subclass for testing AbstractCluedoTile
    static class ConcreteTestCluedoTile extends AbstractCluedoTile {
        protected ConcreteTestCluedoTile(int row, int col) {
            super(row, col);
        }
        // Inherits all concrete methods from AbstractCluedoTile
    }

    @BeforeEach
    void setUp() {
        tile = new ConcreteTestCluedoTile(3, 4);
        player1 = new Player<>(1, "P1", PlayerColor.RED, new GridPos(3, 4));
        player2 = new Player<>(2, "P2", PlayerColor.BLUE, new GridPos(3, 4));
        mockObserver = Mockito.mock(TileObserver.class);
        tile.addObserver(mockObserver);
    }

    @Test
    void constructor_setsRowAndColCorrectly() {
        assertEquals(3, tile.row());
        assertEquals(4, tile.col());
        assertTrue(tile.isWalkable(), "Tile should be walkable by default");
        assertTrue(tile.getPlayers().isEmpty());
    }

    @Test
    void addPlayer_addsPlayerAndNotifiesObservers() {
        tile.addPlayer(player1);
        assertTrue(tile.getPlayers().contains(player1));
        assertEquals(1, tile.getPlayers().size());
        verify(mockObserver, times(1)).onTileChanged(tile);
    }

    @Test
    void addPlayer_multiplePlayers_addsAllAndNotifiesForEach() {
        tile.addPlayer(player1);
        tile.addPlayer(player2);
        assertTrue(tile.getPlayers().contains(player1));
        assertTrue(tile.getPlayers().contains(player2));
        assertEquals(2, tile.getPlayers().size());
        verify(mockObserver, times(2)).onTileChanged(tile);
    }

    @Test
    void removePlayer_removesPlayerAndNotifiesObservers() {
        tile.addPlayer(player1);
        Mockito.reset(mockObserver); // Reset after initial add notification

        tile.removePlayer(player1);
        assertFalse(tile.getPlayers().contains(player1));
        assertTrue(tile.getPlayers().isEmpty());
        verify(mockObserver, times(1)).onTileChanged(tile);
    }

    @Test
    void removePlayer_playerNotOnTile_doesNothingAndNotifiesNothing() {
        tile.addPlayer(player1);
        Mockito.reset(mockObserver);

        tile.removePlayer(player2); // Player2 was never added
        assertTrue(tile.getPlayers().contains(player1));
        assertEquals(1, tile.getPlayers().size());
        verify(mockObserver, times(0)).onTileChanged(tile);
    }

    @Test
    void getIdentifier_returnsCorrectFormat() {
        assertEquals("ConcreteTestCluedoTile(3,4)", tile.getIdentifier());
    }

    @Test
    void addAndRemoveObserver_managesObserversCorrectly() {
        ConcreteTestCluedoTile newTile = new ConcreteTestCluedoTile(1,1);
        TileObserver<GridPos> obs1 = Mockito.mock(TileObserver.class);
        TileObserver<GridPos> obs2 = Mockito.mock(TileObserver.class);

        newTile.addObserver(obs1);
        newTile.addObserver(obs2);

        newTile.addPlayer(player1); // Trigger notification
        verify(obs1, times(1)).onTileChanged(newTile);
        verify(obs2, times(1)).onTileChanged(newTile);

        newTile.removeObserver(obs1);
        newTile.addPlayer(player2); // Trigger another notification
        verify(obs1, times(1)).onTileChanged(newTile); // Should not be called again
        verify(obs2, times(2)).onTileChanged(newTile); // Should be called again
    }

    @Test
    void getPlayers_returnsCopyOfPlayerList() {
        tile.addPlayer(player1);
        List<Player<GridPos>> playersOnTile = tile.getPlayers();
        assertEquals(1, playersOnTile.size());
        assertTrue(playersOnTile.contains(player1));

        // Try to modify the returned list (should fail if it's truly a copy or unmodifiable)
        assertThrows(UnsupportedOperationException.class, () -> playersOnTile.add(player2));

        // Ensure original list in tile is unchanged by the attempt
        assertEquals(1, tile.getPlayers().size());
        assertFalse(tile.getPlayers().contains(player2));
    }

    @Test
    void setWalkable_and_isWalkable_workCorrectly() {
        assertTrue(tile.isWalkable(), "Default walkable state should be true.");
        tile.setWalkable(false);
        assertFalse(tile.isWalkable());
        tile.setWalkable(true);
        assertTrue(tile.isWalkable());
    }
}
