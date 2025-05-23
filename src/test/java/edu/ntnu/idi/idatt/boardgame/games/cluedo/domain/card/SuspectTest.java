package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.exception.InvalidCardException;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Suspect}.
 */
class SuspectTest {

  @Nested
  @DisplayName("names()")
  class Names {

    @Test
    @DisplayName("returns every display name in enum-declaration order")
    void returnsNamesInOrder() {
      String[] names = Suspect.names();
      Suspect[] values = Suspect.values();

      assertEquals(values.length, names.length, "Array size should match enum size");

      IntStream.range(0, values.length)
          .forEach(
              i ->
                  assertEquals(
                      values[i].getName(),
                      names[i],
                      "names()["
                          + i
                          + "] should equal displayName() of the "
                          + i
                          + "-th enum constant"));
    }
  }

  @Nested
  @DisplayName("from(PlayerColor)")
  class From {

    @Test
    @DisplayName("maps every colour used in the enum")
    void mapsEveryDefinedColour() {
      for (Suspect s : Suspect.values()) {
        assertEquals(
            s,
            Suspect.from(s.colour()),
            "Suspect.from(colour) should return the enum constant with that colour");
      }
    }

    @Test
    @DisplayName("throws when colour is not mapped")
    void throwsForUnknownColour() {
      assertThrows(
          InvalidCardException.class,
          () -> Suspect.from(PlayerColor.ORANGE),
          "ORANGE is not mapped to a Suspect and should cause an exception");
    }

    @Test
    @DisplayName("throws when colour is null")
    void throwsForNull() {
      assertThrows(
          NullPointerException.class,
          () -> Suspect.from(null),
          "Passing null should result in a NullPointerException");
    }
  }
}
