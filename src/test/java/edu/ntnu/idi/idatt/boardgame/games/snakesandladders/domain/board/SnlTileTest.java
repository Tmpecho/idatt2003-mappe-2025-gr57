package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SnlTileTest {

  private SnlTile tile;
  private Player<LinearPos> player1;
  private Player<LinearPos> player2;
  private TileObserver<LinearPos> mockObserver;

  @BeforeEach
  void setUp() {
    tile = new SnlTile(5);
    player1 = new Player<>(1, "P1", PlayerColor.RED, new LinearPos(5));
    player2 = new Player<>(2, "P2", PlayerColor.BLUE, new LinearPos(5));
    mockObserver = Mockito.mock(TileObserver.class);
    tile.addObserver(mockObserver);
  }

  @Test
  void constructor_initializesWithCorrectPosition() {
    assertEquals(5, tile.getPosition());
    assertTrue(tile.getPlayers().isEmpty());
  }

  @Test
  void addPlayer_addsPlayerToListAndNotifiesObserver() {
    tile.addPlayer(player1);
    assertTrue(tile.getPlayers().contains(player1));
    assertEquals(1, tile.getPlayers().size());
    verify(mockObserver, times(1)).onTileChanged(tile);
  }

  @Test
  void addPlayer_doesNotAddDuplicatePlayerAndNotifiesOnceForFirstAdd() {
    tile.addPlayer(player1);
    tile.addPlayer(player1);
    assertEquals(1, tile.getPlayers().size());
    verify(mockObserver, times(1)).onTileChanged(tile);
  }

  @Test
  void removePlayer_removesPlayerFromListAndNotifiesObserver() {
    tile.addPlayer(player1);
    Mockito.reset(mockObserver);

    tile.removePlayer(player1);
    assertFalse(tile.getPlayers().contains(player1));
    assertTrue(tile.getPlayers().isEmpty());
    verify(mockObserver, times(1)).onTileChanged(tile);
  }

  @Test
  void removePlayer_doesNothingIfPlayerNotInListAndDoesNotNotify() {
    tile.addPlayer(player1);
    Mockito.reset(mockObserver);

    tile.removePlayer(player2);
    assertTrue(tile.getPlayers().contains(player1));
    assertEquals(1, tile.getPlayers().size());
    verify(mockObserver, times(0)).onTileChanged(tile);
  }

  @Test
  void getIdentifier_returnsCorrectFormat() {
    assertEquals("Tile #5", tile.getIdentifier());
  }

  @Test
  void getPlayers_returnsUnmodifiableListOfPlayers() {
    tile.addPlayer(player1);
    var players = tile.getPlayers();
    assertThrows(UnsupportedOperationException.class, () -> players.add(player2));
  }

  @Test
  void addAndRemoveObserver() {
    SnlTile newTile = new SnlTile(10);
    TileObserver<LinearPos> newMockObserver = Mockito.mock(TileObserver.class);

    newTile.addObserver(newMockObserver);
    newTile.addPlayer(player1);
    verify(newMockObserver, times(1)).onTileChanged(newTile);

    newTile.removeObserver(newMockObserver);
    newTile.removePlayer(player1);
    verify(newMockObserver, times(1)).onTileChanged(newTile);
  }
}
