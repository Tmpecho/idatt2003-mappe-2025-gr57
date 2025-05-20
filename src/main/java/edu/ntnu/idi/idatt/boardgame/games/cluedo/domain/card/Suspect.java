package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.Arrays;

public enum Suspect {
  MISS_SCARLETT(PlayerColor.WHITE, "Miss Scarlett"),
  COLONEL_MUSTARD(PlayerColor.RED, "Col. Mustard"),
  MRS_WHITE(PlayerColor.YELLOW, "Mrs. White"),
  REVEREND_GREEN(PlayerColor.GREEN, "Rev. Green"),
  MRS_PEACOCK(PlayerColor.BLUE, "Mrs. Peacock"),
  PROFESSOR_PLUM(PlayerColor.PURPLE, "Prof. Plum");

  private final PlayerColor colour;
  private final String displayName;

  Suspect(PlayerColor colour, String displayName) {
    this.colour = colour;
    this.displayName = displayName;
  }

  /** Convenience: every suspect’s display name in game-order (needed by {@code Cards}). */
  public static String[] names() {
    return Arrays.stream(values()).map(Suspect::displayName).toArray(String[]::new);
  }

  public static Suspect from(PlayerColor playerColor) {
    return Arrays.stream(values())
        .filter(suspect -> suspect.colour == playerColor)
        .findFirst()
        .orElseThrow(
            () -> {
              LoggingNotification.error(
                  "No suspect for color", "No suspect found for " + playerColor);
              return new IllegalArgumentException("No suspect for " + playerColor);
            });
  }

  public PlayerColor colour() {
    return colour;
  }

  public String displayName() {
    return displayName;
  }
}
