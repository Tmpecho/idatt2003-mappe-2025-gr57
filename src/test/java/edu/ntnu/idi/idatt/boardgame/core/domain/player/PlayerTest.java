package edu.ntnu.idi.idatt.boardgame.core.domain.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {

  private final LinearPos initialLinearPos = new LinearPos(1);
  private final GridPos initialGridPos = new GridPos(0, 0);
  private Player<LinearPos> linearPlayer;
  private Player<GridPos> gridPlayer;

  @BeforeEach
  void setUp() {
    linearPlayer = new Player<>(1, "Alice", PlayerColor.RED, initialLinearPos);
    gridPlayer = new Player<>(2, "Bob", PlayerColor.BLUE, initialGridPos);
  }

  @Test
  void constructor_setsFieldsCorrectly_forLinearPos() {
    assertEquals(1, linearPlayer.getId());
    assertEquals("Alice", linearPlayer.getName());
    assertEquals(PlayerColor.RED, linearPlayer.getColor());
    assertEquals(initialLinearPos, linearPlayer.getPosition());
  }

  @Test
  void constructor_setsFieldsCorrectly_forGridPos() {
    assertEquals(2, gridPlayer.getId());
    assertEquals("Bob", gridPlayer.getName());
    assertEquals(PlayerColor.BLUE, gridPlayer.getColor());
    assertEquals(initialGridPos, gridPlayer.getPosition());
  }

  @Test
  void setPosition_updatesPosition_forLinearPos() {
    LinearPos newPos = new LinearPos(10);
    linearPlayer.setPosition(newPos);
    assertEquals(newPos, linearPlayer.getPosition());
  }

  @Test
  void setPosition_updatesPosition_forGridPos() {
    GridPos newPos = new GridPos(5, 5);
    gridPlayer.setPosition(newPos);
    assertEquals(newPos, gridPlayer.getPosition());
  }

  @Test
  void getPosition_returnsCurrentPosition() {
    assertEquals(initialLinearPos, linearPlayer.getPosition());
    assertEquals(initialGridPos, gridPlayer.getPosition());
  }

  @Test
  void getId_returnsCorrectId() {
    assertEquals(1, linearPlayer.getId());
    assertEquals(2, gridPlayer.getId());
  }

  @Test
  void getName_returnsCorrectName() {
    assertEquals("Alice", linearPlayer.getName());
    assertEquals("Bob", gridPlayer.getName());
  }

  @Test
  void getColor_returnsCorrectColor() {
    assertEquals(PlayerColor.RED, linearPlayer.getColor());
    assertEquals(PlayerColor.BLUE, gridPlayer.getColor());
  }

  @Test
  void toString_returnsExpectedFormat() {
    String expectedLinear = "Player{id=1, name=Alice, position=index: 1, color=RED}";
    assertEquals(expectedLinear, linearPlayer.toString());

    String expectedGrid = "Player{id=2, name=Bob, position=[0, 0], color=BLUE}";
    assertEquals(expectedGrid, gridPlayer.toString());
  }
}
