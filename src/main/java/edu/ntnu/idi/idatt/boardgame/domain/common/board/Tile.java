package edu.ntnu.idi.idatt.boardgame.domain.common.board;

import edu.ntnu.idi.idatt.boardgame.domain.common.player.Player;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class Tile {
  private final StackPane tile;
  private final Label posLabel;
  private final HBox playerBox;
  private final VBox container;
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
    container = new VBox();
    container.setAlignment(Pos.CENTER);
    container.setSpacing(5);
    container.getChildren().add(posLabel);

    // The rectangle representing the tile's background
    Rectangle rect = new Rectangle(tileSize, tileSize);
    rect.setFill(Color.BEIGE);
    rect.setStroke(Color.DARKGRAY);
    rect.setArcWidth(5);
    rect.setArcHeight(5);

    // Build the overall StackPane
    tile = new StackPane(rect, container);
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
    container.getChildren().clear();
    container.getChildren().add(posLabel);

    playerBox.getChildren().clear();

    // If there are players, add their icons and then add the playerBox to the container
    if (players.isEmpty()) {
      return;
    }
    for (Player player : players) {
      playerBox.getChildren().add(player.getIcon());
    }
    container.getChildren().add(playerBox);
  }
}
