package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.exception.InvalidCardException;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.Arrays;

/**
 * Enum representing the suspects in the Cluedo game. Each suspect has an associated
 * {@link PlayerColor} and a display name.
 */
public enum Suspect implements Card {
  /**
   * Miss Scarlett, associated with {@link PlayerColor#WHITE}.
   */
  MISS_SCARLETT(PlayerColor.WHITE, "Miss Scarlett"),
  /**
   * Colonel Mustard, associated with {@link PlayerColor#RED}.
   */
  COLONEL_MUSTARD(PlayerColor.RED, "Col. Mustard"),
  /**
   * Mrs. White, associated with {@link PlayerColor#YELLOW}.
   */
  MRS_WHITE(PlayerColor.YELLOW, "Mrs. White"),
  /**
   * Reverend Green, associated with {@link PlayerColor#GREEN}.
   */
  REVEREND_GREEN(PlayerColor.GREEN, "Rev. Green"),
  /**
   * Mrs. Peacock, associated with {@link PlayerColor#BLUE}.
   */
  MRS_PEACOCK(PlayerColor.BLUE, "Mrs. Peacock"),
  /**
   * Professor Plum, associated with {@link PlayerColor#PURPLE}.
   */
  PROFESSOR_PLUM(PlayerColor.PURPLE, "Prof. Plum");

  private final PlayerColor colour;
  private final String displayName;

  Suspect(PlayerColor colour, String displayName) {
    this.colour = colour;
    this.displayName = displayName;
  }

  /**
   * Convenience: every suspect’s display name in game-order (needed by {@code Cards}).
   *
   * @return An array of display names for all suspects.
   */
  public static String[] names() {
    return Arrays.stream(values()).map(Suspect::getName).toArray(String[]::new);
  }

  /**
   * Maps a {@link PlayerColor} to its {@code Suspect}.
   *
   * @param playerColor The player color to map.
   * @return The {@link Suspect} corresponding to the given color.
   * @throws NullPointerException if {@code playerColor} is {@code null}
   * @throws InvalidCardException if the colour is not used by any suspect
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
              return new InvalidCardException("No suspect for " + playerColor);
            });
  }

  /**
   * Returns the {@link PlayerColor} associated with this suspect.
   *
   * @return The player color.
   */
  public PlayerColor colour() {
    return colour;
  }

  /**
   * Returns the display name of this suspect.
   *
   * @return The display name.
   */
  @Override
  public String getName() {
    return displayName;
  }
}
