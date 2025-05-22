package edu.ntnu.idi.idatt.boardgame.ui.util;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.exception.UnknownPlayerColorException;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Utility class for mapping {@link PlayerColor} enum to JavaFX {@link Paint} objects. This class
 * cannot be instantiated.
 */
public final class PlayerColorMapper {

  /**
   * Private constructor to prevent instantiation.
   */
  private PlayerColorMapper() {
    // Utility class
  }

  /**
   * Converts a {@link PlayerColor} to its corresponding JavaFX {@link Paint} object.
   *
   * @param playerColor The player color to convert.
   * @return The JavaFX Paint object representing the color.
   * @throws UnknownPlayerColorException if the player color is not recognized.
   */
  public static Paint toPaint(PlayerColor playerColor) {
    return switch (playerColor) {
      case WHITE -> Color.WHITE;
      case RED -> Color.RED;
      case BLUE -> Color.BLUE;
      case GREEN -> Color.GREEN;
      case YELLOW -> Color.YELLOW;
      case ORANGE -> Color.ORANGE;
      case PURPLE -> Color.PURPLE;
      default -> throw new UnknownPlayerColorException("Unrecognized player color: " + playerColor);
    };
  }
}
