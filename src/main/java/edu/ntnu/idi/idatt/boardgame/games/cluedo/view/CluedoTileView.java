package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class CluedoTileView implements TileObserver {
    private final StackPane tilePane;
    private final Rectangle tileBackground;
    private final Label infoLabel; // For room names or coordinates
    private final FlowPane playerPane; // Use FlowPane for better wrapping
    private final AbstractCluedoTile tileModel;
    private final int tileSize;

    public CluedoTileView(AbstractCluedoTile tileModel, int tileSize) {
        this.tileModel = tileModel;
        this.tileSize = tileSize;
        tileModel.addObserver(this);

        tileBackground = new Rectangle(tileSize, tileSize);
        tileBackground.setStroke(Color.DARKGRAY);
        tileBackground.setArcWidth(3);
        tileBackground.setArcHeight(3);

        infoLabel = new Label();
        infoLabel.setFont(Font.font(Math.max(8, tileSize * 0.15))); // Smaller font
        infoLabel.setTextAlignment(TextAlignment.CENTER);
        infoLabel.setWrapText(true);

        playerPane = new FlowPane(); // Use FlowPane
        playerPane.setAlignment(Pos.CENTER);
        playerPane.setHgap(1); // Small horizontal gap
        playerPane.setVgap(1); // Small vertical gap
        playerPane.setPrefWrapLength(tileSize - 4); // Allow wrapping within tile width

        tilePane = new StackPane(tileBackground, infoLabel, playerPane);
        StackPane.setAlignment(infoLabel, Pos.TOP_CENTER); // Position label at top
        StackPane.setAlignment(playerPane, Pos.CENTER); // Center players below label
        tilePane.setAlignment(Pos.CENTER);

        updateDisplay();
    }

    public StackPane getNode() {
        return tilePane;
    }

    @Override
    public void onTileChanged(Tile<?> tile) {
        if (tile == this.tileModel) {
            updateDisplay();
        }
    }

    void updateDisplay() {
        playerPane.getChildren().clear(); // Clear previous player circles

        // Set background and label based on tile type
        if (tileModel instanceof RoomTile roomTile) {
            tileBackground.setFill(Color.LIGHTSLATEGRAY); // Room color
            infoLabel.setText(roomTile.getRoomName());
            infoLabel.setVisible(true);
        } else if (tileModel instanceof CorridorTile) {
            tileBackground.setFill(Color.BEIGE); // Corridor color
            // Optionally show coordinates for corridors, or leave blank
            // infoLabel.setText(String.format("(%d,%d)", tileModel.row(),
            // tileModel.col()));
            infoLabel.setVisible(false); // Hide label for corridors for cleaner look
        } else {
            tileBackground.setFill(Color.WHITE); // Default / Unknown
            infoLabel.setText("");
            infoLabel.setVisible(false);
        }

        // Add player circles if any players are on this tile
        if (!tileModel.getPlayers().isEmpty()) {
            tileModel.getPlayers().forEach(player -> {
                Circle circle = new Circle(Math.max(3, tileSize * 0.1)); // Smaller player circle
                circle.setFill(PlayerColorMapper.toPaint(player.getColor()));
                circle.setStroke(Color.BLACK); // Add border for visibility
                circle.setStrokeWidth(0.5);
                playerPane.getChildren().add(circle);
            });
        }
    }
}
