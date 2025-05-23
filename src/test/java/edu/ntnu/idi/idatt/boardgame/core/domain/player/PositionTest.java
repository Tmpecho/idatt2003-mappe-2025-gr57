package edu.ntnu.idi.idatt.boardgame.core.domain.player;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PositionTest {

  @Test
  void linearPos_toString_returnsCorrectFormat() {
    LinearPos pos = new LinearPos(5);
    assertEquals("index: 5", pos.toString());
  }

  @Test
  void linearPos_equalsAndHashCode_workAsExpectedForRecord() {
    LinearPos pos1 = new LinearPos(10);
    LinearPos pos2 = new LinearPos(10);
    LinearPos pos3 = new LinearPos(20);

    assertEquals(pos1, pos2);
    assertNotEquals(pos1, pos3);
    assertEquals(pos1.hashCode(), pos2.hashCode());
    assertNotEquals(pos1.hashCode(), pos3.hashCode());
  }

  @Test
  void gridPos_toString_returnsCorrectFormat() {
    GridPos pos = new GridPos(2, 3);
    assertEquals("[2, 3]", pos.toString());
  }

  @Test
  void gridPos_equalsAndHashCode_workAsExpectedForRecord() {
    GridPos pos1 = new GridPos(1, 1);
    GridPos pos2 = new GridPos(1, 1);
    GridPos pos3 = new GridPos(1, 2);
    GridPos pos4 = new GridPos(2, 1);

    assertEquals(pos1, pos2);
    assertNotEquals(pos1, pos3);
    assertNotEquals(pos1, pos4);
    assertEquals(pos1.hashCode(), pos2.hashCode());
    assertNotEquals(pos1.hashCode(), pos3.hashCode());
  }
}
