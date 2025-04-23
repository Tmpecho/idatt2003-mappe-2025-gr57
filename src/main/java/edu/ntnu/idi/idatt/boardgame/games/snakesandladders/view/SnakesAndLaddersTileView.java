package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnakesAndLaddersTile;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class SnakesAndLaddersTileView implements TileObserver {
  private final StackPane tilePane;
  private final Label posLabel;
  private final HBox playerBox;
  private final VBox tileContainer;
  private final SnakesAndLaddersTile tileModel;

  public SnakesAndLaddersTileView(SnakesAndLaddersTile tileModel, int tileSize) {
    this.tileModel = tileModel;
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

  public StackPane getNode() {
    return tilePane;
  }

  @Override
  public void onTileChanged(Tile tile) {
    if (tile == this.tileModel) {
      updateDisplay();
    }
  }

  void updateDisplay() {
    tileContainer.getChildren().clear();
    tileContainer.getChildren().add(posLabel);
    playerBox.getChildren().clear();

    if (tileModel.getPlayers().isEmpty()) {
      return;
    }

    tileModel
        .getPlayers()
        .forEach(
            player -> {
              Circle circle = new Circle(7);
              circle.setFill(PlayerColor.mapToJavaFXColor(player.getColor()));
              playerBox.getChildren().add(circle);
            });
    tileContainer.getChildren().add(playerBox);
  }
}
