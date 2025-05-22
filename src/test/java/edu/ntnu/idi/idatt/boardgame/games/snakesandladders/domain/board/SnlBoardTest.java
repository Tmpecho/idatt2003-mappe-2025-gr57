package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SnlBoardTest {

  private SnlBoard board;
  private Player<LinearPos> player1;
  private Player<LinearPos> player2;
  private Map<Integer, Player<LinearPos>> playersMap;

  @BeforeEach
  void setUp() {
    board = new SnlBoard();
    player1 = new Player<>(1, "P1", PlayerColor.RED, new LinearPos(1));
    player2 = new Player<>(2, "P2", PlayerColor.BLUE, new LinearPos(1));
    playersMap = new HashMap<>();
    playersMap.put(player1.getId(), player1);
    playersMap.put(player2.getId(), player2);
  }

  @Test
  void constructor_initializesBoardWithCorrectSizeAndConnectors() {
    assertEquals(90, board.getBoardSize());
    assertEquals(90, board.getTiles().size());

    Connector snakeAt30 =
        board.getConnectors().stream()
            .filter(c -> c.getStart() == 30 && c instanceof Snake)
            .findFirst()
            .orElse(null);
    assertNotNull(snakeAt30);
    assertEquals(30 - 14, snakeAt30.getEnd());

    Connector ladderAt8 =
        board.getConnectors().stream()
            .filter(c -> c.getStart() == 8 && c instanceof Ladder)
            .findFirst()
            .orElse(null);
    assertNotNull(ladderAt8);
    assertEquals(8 + 6, ladderAt8.getEnd());
  }

  @Test
  void addPlayersToStart_placesPlayersAtPosition1() {
    board.addPlayersToStart(playersMap);
    assertEquals(new LinearPos(1), player1.getPosition());
    assertEquals(new LinearPos(1), player2.getPosition());
    assertTrue(board.getTiles().get(1).getPlayers().contains(player1));
    assertTrue(board.getTiles().get(1).getPlayers().contains(player2));
  }

  @Test
  void incrementPlayerPosition_normalMovePlayer() {
    board.addPlayersToStart(playersMap);
    board.incrementPlayerPosition(player1, 5);
    assertEquals(new LinearPos(6), player1.getPosition());
    assertTrue(board.getTiles().get(6).getPlayers().contains(player1));
    assertFalse(board.getTiles().get(1).getPlayers().contains(player1));
  }

  @Test
  void incrementPlayerPosition_landsOnSnakeHead() {
    board.addPlayersToStart(playersMap);
    player1.setPosition(new LinearPos(25));
    board.getTiles().get(1).removePlayer(player1);
    board.getTiles().get(25).addPlayer(player1);

    board.incrementPlayerPosition(player1, 5);
    assertEquals(new LinearPos(16), player1.getPosition());
    assertTrue(board.getTiles().get(16).getPlayers().contains(player1));
    assertFalse(board.getTiles().get(30).getPlayers().contains(player1));
  }

  @Test
  void incrementPlayerPosition_landsOnLadderBottom() {
    board.addPlayersToStart(playersMap);
    player1.setPosition(new LinearPos(3));
    board.getTiles().get(1).removePlayer(player1);
    board.getTiles().get(3).addPlayer(player1);

    board.incrementPlayerPosition(player1, 5);
    assertEquals(new LinearPos(14), player1.getPosition());
    assertTrue(board.getTiles().get(14).getPlayers().contains(player1));
    assertFalse(board.getTiles().get(8).getPlayers().contains(player1));
  }

  @Test
  void incrementPlayerPosition_overshootsBoardAndBouncesBack() {
    board.addPlayersToStart(playersMap);
    player1.setPosition(new LinearPos(88));
    board.getTiles().get(1).removePlayer(player1);
    board.getTiles().get(88).addPlayer(player1);

    board.incrementPlayerPosition(player1, 3);
    assertEquals(new LinearPos(89), player1.getPosition());
    assertTrue(board.getTiles().get(89).getPlayers().contains(player1));
  }

  @Test
  void incrementPlayerPosition_landsExactlyOnBoardEnd() {
    board.addPlayersToStart(playersMap);
    player1.setPosition(new LinearPos(85));
    board.getTiles().get(1).removePlayer(player1);
    board.getTiles().get(85).addPlayer(player1);

    board.incrementPlayerPosition(player1, 5);
    assertEquals(new LinearPos(90), player1.getPosition());
    assertTrue(board.getTiles().get(90).getPlayers().contains(player1));
  }

  @Test
  void setPlayerPosition_movesPlayerToCorrectTile() {
    board.addPlayersToStart(playersMap);
    LinearPos newPos = new LinearPos(15);
    board.setPlayerPosition(player1, newPos);

    assertEquals(newPos, player1.getPosition());
    assertTrue(board.getTiles().get(15).getPlayers().contains(player1));
    assertFalse(board.getTiles().get(1).getPlayers().contains(player1));
  }

  @Test
  void getRowsAndCols_returnsCorrectValues() {
    assertEquals(10, board.getRows());
    assertEquals(9, board.getCols());
  }
}
