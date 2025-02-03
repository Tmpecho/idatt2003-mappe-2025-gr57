package edu.idi.idatt;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class TileGrid extends GridPane {

  public TileGrid(int rows, int cols, int tileSize) {

    this.setHgap(10);
    this.setVgap(10);
    this.setPadding(new Insets(10));

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Label tile = new Label();
        tile.setPrefSize(tileSize, tileSize);
        tile.setStyle("-fx-border-color: black; -fx-background-color: white");
        this.add(tile, col, row);
      }
    }
  }
}
