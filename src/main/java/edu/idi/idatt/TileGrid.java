package edu.idi.idatt;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class TileGrid extends GridPane {

  public TileGrid(int rows, int cols, int tileSize) {

    this.setHgap(10);
    this.setVgap(10);

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Button tile = new Button();
        tile.setPrefSize(tileSize, tileSize);
        this.add(tile, col, row);
      }
    }
  }
}
