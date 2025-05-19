package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
class ConnectorTest {

  @Test
  void snake_constructorAndGetters_workCorrectly() {
    Snake snake = new Snake(30, 14);
    assertEquals(30, snake.getStart());
    assertEquals(16, snake.getEnd());
    assertEquals(Color.RED, snake.getColor());
    assertEquals("Snake", snake.getConnectorType());
  }

  @Test
  void ladder_constructorAndGetters_workCorrectly() {
    Ladder ladder = new Ladder(8, 6);
    assertEquals(8, ladder.getStart());
    assertEquals(14, ladder.getEnd());
    assertEquals(Color.GREEN, ladder.getColor());
    assertEquals("Ladder", ladder.getConnectorType());
  }
}
