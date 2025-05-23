package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnlTile;
import edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/**
 * Represents the visual view of a single tile on the Snakes and Ladders board. Implements
 * {@link TileObserver} to update its display when the underlying tile model changes.
 */
public final class SnlTileView implements TileObserver<LinearPos> {

  private final StackPane tilePane;
  private final Label posLabel;
  private final HBox playerBox;
  private final VBox tileContainer;
  private final SnlTile tileModel;
  private final int tileSize;

  /**
   * Constructs an SnLTileView.
   *
   * @param tileModel The {@link SnlTile} this view represents.
   * @param tileSize  The size (width and height) of the tile in pixels.
   */
  public SnlTileView(SnlTile tileModel, int tileSize) {
    this.tileModel = tileModel;
    this.tileSize = tileSize;
    tileModel.addObserver(this);

    posLabel = new Label(String.valueOf(tileModel.getPosition()));
    posLabel.setFont(Font.font(10));

    playerBox = new HBox();
    playerBox.setAlignment(Pos.CENTER);
    playerBox.setSpacing(-5);

    tileContainer = new VBox();
    tileContainer.setAlignment(Pos.CENTER);
    tileContainer.setSpacing(5);
    tileContainer.getChildren().add(posLabel);

    Rectangle tileBackground = new Rectangle(tileSize, tileSize);
    tileBackground.setFill(Color.BEIGE);
    tileBackground.setStroke(Color.DARKGRAY);
    tileBackground.setArcWidth(5);
    tileBackground.setArcHeight(5);

    tilePane = new StackPane(tileBackground, tileContainer);
    tilePane.setAlignment(Pos.CENTER);

    updateDisplay();
  }

  /**
   * Gets the JavaFX {@link StackPane} node representing this tile view.
   *
   * @return The StackPane node.
   */
  public StackPane getNode() {
    return tilePane;
  }

  @Override
  public void onTileChanged(Tile<LinearPos> tile) {
    if (tile == this.tileModel) {
      updateDisplay();
    }
  }

  /**
   * Updates the visual display of the tile based on the state of its model. This includes
   * displaying the tile number and any player tokens on it.
   */
  void updateDisplay() {
    tileContainer.getChildren().clear();
    tileContainer.getChildren().add(posLabel);
    playerBox.getChildren().clear();

    tileModel
        .getPlayers()
        .forEach(
            player -> {
              Circle circle = new Circle(7);
              circle.setFill(PlayerColorMapper.toPaint(player.getColor()));
              playerBox.getChildren().add(circle);
            });
    tileContainer.getChildren().add(playerBox);
  }
}
