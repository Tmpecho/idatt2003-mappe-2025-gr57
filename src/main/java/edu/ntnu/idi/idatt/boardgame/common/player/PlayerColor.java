package edu.ntnu.idi.idatt.boardgame.common.player;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum PlayerColor {
	RED,
	BLUE,
	GREEN,
	YELLOW,
	ORANGE,
	PURPLE;

	public static Paint mapToJavaFXColor(PlayerColor color) {
		return switch (color) {
			case RED -> Color.RED;
			case BLUE -> Color.BLUE;
			case GREEN -> Color.GREEN;
			case YELLOW -> Color.YELLOW;
			case ORANGE -> Color.ORANGE;
			case PURPLE -> Color.PURPLE;
		};
	}
}