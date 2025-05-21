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
    return Arrays.stream(values()).map(Suspect::getName).toArray(String[]::new);
  }

  /**
   * Maps a {@link PlayerColor} to its {@code Suspect}.
   *
   * @throws NullPointerException if {@code playerColor} is {@code null}
   * @throws IllegalArgumentException if the colour is not used by any suspect
   */
  public static Suspect from(PlayerColor playerColor) {
    if (playerColor == null) {
      throw new NullPointerException("playerColor is null");
    }

    return Arrays.stream(values())
        .filter(s -> s.colour == playerColor)
        .findFirst()
        .orElseThrow(
            () -> {
              // swallow any JavaFX-initialization problems
              try {
                LoggingNotification.error("Unknown colour", "No suspect for " + playerColor);
              } catch (RuntimeException ignored) {
              }
              return new IllegalArgumentException("No suspect for " + playerColor);
            });
  }

  public PlayerColor colour() {
    return colour;
  }

  public String getName() {
    return displayName;
  }
}
