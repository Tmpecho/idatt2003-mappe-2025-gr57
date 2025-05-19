package edu.ntnu.idi.idatt.boardgame.ui.util;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class PlayerTokenFactory {
  private static final double MIN_CIRCLE_RADIUS = 3.0;
  private static final double CIRCLE_STROKE_WIDTH = 0.5;

  public static Circle createPlayerToken(Player<GridPos> player, double size, double radiusFactor) {
    Circle playerToken = new Circle(Math.max(MIN_CIRCLE_RADIUS, size * radiusFactor));
    playerToken.setFill(PlayerColorMapper.toPaint(player.getColor()));
    playerToken.setStroke(Color.BLACK);
    playerToken.setStrokeWidth(CIRCLE_STROKE_WIDTH);
    return playerToken;
  }
}
