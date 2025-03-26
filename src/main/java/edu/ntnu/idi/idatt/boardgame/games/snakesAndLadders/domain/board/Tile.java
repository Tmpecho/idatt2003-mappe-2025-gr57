package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board;

import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import edu.ntnu.idi.idatt.boardgame.common.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public
class Tile { // todo: make this class implement a common interface for all tiles so we can have
  // different types of tiles
  private final StackPane tile;
  private final Label posLabel;
  private final HBox playerBox;
  private final VBox tileContainer;
  private final List<Player> players;

  /**
   * Constructs a Tile at a given board position with a specified size. The tile is initially empty
   * (no players).
   *
   * @param pos the board position (e.g. 1, 2, 3, …)
   * @param tileSize the size (width and height) of the tile
   */
  public Tile(int pos, int tileSize) {
    this.players = new ArrayList<>();

    // Label showing the tile's position
    posLabel = new Label(String.valueOf(pos));
    posLabel.setFont(Font.font(10));

    // HBox for displaying players (their graphical representations)
    playerBox = new HBox();
    playerBox.setAlignment(Pos.CENTER);
    playerBox.setSpacing(-5);

    // Container holding the position label and, if any, the playerBox.
    tileContainer = new VBox();
    tileContainer.setAlignment(Pos.CENTER);
    tileContainer.setSpacing(5);
    tileContainer.getChildren().add(posLabel);

    Rectangle tileBackground = new Rectangle(tileSize, tileSize);
    tileBackground.setFill(Color.BEIGE);
    tileBackground.setStroke(Color.DARKGRAY);
    tileBackground.setArcWidth(5);
    tileBackground.setArcHeight(5);

    tile = new StackPane(tileBackground, tileContainer);
    tile.setAlignment(Pos.CENTER);
  }

  /**
   * Returns the JavaFX node representing this tile.
   *
   * @return the StackPane for the tile
   */
  public StackPane getTile() {
    return tile;
  }

  /**
   * Adds a player to the tile if it is not already present, then updates the display.
   *
   * @param player the player to add
   */
  public void addPlayer(Player player) {
    if (!players.contains(player)) {
      players.add(player);
      updateDisplay();
    }
  }

  /**
   * Removes a player from the tile and updates the display.
   *
   * @param player the player to remove
   */
  public void removePlayer(Player player) {
    if (players.remove(player)) {
      updateDisplay();
    }
  }

  /**
   * Updates the display. Always shows the position label. If there are players on the tile, their
   * icons are added to the playerBox.
   */
  private void updateDisplay() {
    tileContainer.getChildren().clear();
    tileContainer.getChildren().add(posLabel);

    playerBox.getChildren().clear();

    // If there are players, add their icons and then add the playerBox to the container
    if (players.isEmpty()) {
      return;
    }
    players.forEach(
        player -> {
          Circle circle = new Circle(7);
          circle.setFill(mapToJavaFXColor(player.getColor()));
          playerBox.getChildren().add(circle);
        });
    tileContainer.getChildren().add(playerBox);
  }

  private Paint mapToJavaFXColor(PlayerColor color) {
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
