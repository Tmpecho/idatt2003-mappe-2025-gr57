package edu.idi.idatt.model;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class Tile {
	private final StackPane tile;

	public Tile(int pos, int tileSize) {
		tile = createTile(pos, tileSize);
	}

	public StackPane getTile() {
		return tile;
	}

	public static StackPane createTile(int pos, int tileSize) {
		Rectangle rect = new Rectangle(tileSize, tileSize);
		rect.setFill(Color.BEIGE);
		rect.setStroke(Color.DARKGRAY);
		rect.setArcWidth(10);
		rect.setArcHeight(10);

		Label label = new Label(String.valueOf(pos));
		label.setFont(Font.font(12));

		StackPane tile = new StackPane(rect, label);
		tile.setAlignment(Pos.CENTER);
		return tile;
	}
}
