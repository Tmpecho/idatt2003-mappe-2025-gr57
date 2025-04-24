
package edu.ntnu.idi.idatt.boardgame.ui.util;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;

public final class PlayerColorMapper {
	public static Paint toPaint(PlayerColor c) {
		return switch(c) {
			case RED    -> Color.RED;
			case BLUE   -> Color.BLUE;
			case GREEN  -> Color.GREEN;
			case YELLOW -> Color.YELLOW;
			case ORANGE -> Color.ORANGE;
			case PURPLE -> Color.PURPLE;
		};
	}
}