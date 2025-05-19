package edu.ntnu.idi.idatt.boardgame.ui.util;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
class PlayerColorMapperTest {

  @Test
  void toPaint_returnsCorrectPaintForWhite() {
    assertEquals(Color.WHITE, PlayerColorMapper.toPaint(PlayerColor.WHITE));
  }

  @Test
  void toPaint_returnsCorrectPaintForRed() {
    assertEquals(Color.RED, PlayerColorMapper.toPaint(PlayerColor.RED));
  }

  @Test
  void toPaint_returnsCorrectPaintForBlue() {
    assertEquals(Color.BLUE, PlayerColorMapper.toPaint(PlayerColor.BLUE));
  }

  @Test
  void toPaint_returnsCorrectPaintForGreen() {
    assertEquals(Color.GREEN, PlayerColorMapper.toPaint(PlayerColor.GREEN));
  }

  @Test
  void toPaint_returnsCorrectPaintForYellow() {
    assertEquals(Color.YELLOW, PlayerColorMapper.toPaint(PlayerColor.YELLOW));
  }

  @Test
  void toPaint_returnsCorrectPaintForOrange() {
    assertEquals(Color.ORANGE, PlayerColorMapper.toPaint(PlayerColor.ORANGE));
  }

  @Test
  void toPaint_returnsCorrectPaintForPurple() {
    assertEquals(Color.PURPLE, PlayerColorMapper.toPaint(PlayerColor.PURPLE));
  }
}
