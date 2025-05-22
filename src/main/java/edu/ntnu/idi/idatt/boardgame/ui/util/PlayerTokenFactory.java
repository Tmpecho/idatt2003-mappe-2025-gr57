package edu.ntnu.idi.idatt.boardgame.ui.util;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Factory class for creating player token visuals (JavaFX {@link Circle} objects). This class
 * cannot be instantiated.
 */
public final class PlayerTokenFactory {

  /**
   * The minimum radius for a player token circle.
   */
  private static final double MIN_CIRCLE_RADIUS = 3.0;
  /**
   * The stroke width for the player token circle's border.
   */
  private static final double CIRCLE_STROKE_WIDTH = 0.5;

  /**
   * Private constructor to prevent instantiation.
   */
  private PlayerTokenFactory() {
    // Utility class
  }

  /**
   * Creates a visual representation of a player token as a JavaFX {@link Circle}. The token's fill
   * color is determined by the player's color, and it has a black stroke.
   *
   * @param player       The player for whom to create the token.
   * @param size         The base size used to calculate the token's radius (e.g., tile size).
   * @param radiusFactor A factor to multiply with {@code size} to determine the radius.
   * @return A {@link Circle} object representing the player token.
   */
  public static Circle createPlayerToken(Player<GridPos> player, double size, double radiusFactor) {
    Circle playerToken = new Circle(Math.max(MIN_CIRCLE_RADIUS, size * radiusFactor));
    playerToken.setFill(PlayerColorMapper.toPaint(player.getColor()));
    playerToken.setStroke(Color.BLACK);
    playerToken.setStrokeWidth(CIRCLE_STROKE_WIDTH);
    return playerToken;
  }
}
