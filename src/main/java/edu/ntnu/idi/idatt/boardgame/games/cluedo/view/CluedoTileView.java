package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.BorderTile;
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

public final class CluedoTileView implements TileObserver<GridPos> {
    private final StackPane tilePane;
    private final Rectangle tileBackground;
    private final Label infoLabel;
    private final FlowPane playerPane;
    private final AbstractCluedoTile tileModel;
    private final int tileSize;
    private boolean isDoorCorridor = false;

    public CluedoTileView(AbstractCluedoTile tileModel, int tileSize) {
        this.tileModel = tileModel;
        this.tileSize = tileSize;
        tileModel.addObserver(this);

        tileBackground = new Rectangle(tileSize, tileSize);
        tileBackground.setStroke(Color.DARKGRAY);
        tileBackground.setArcWidth(3);
        tileBackground.setArcHeight(3);

        infoLabel = new Label();
        infoLabel.setFont(Font.font(Math.max(8, tileSize * 0.15)));
        infoLabel.setTextAlignment(TextAlignment.CENTER);
        infoLabel.setWrapText(true);

        playerPane = new FlowPane();
        playerPane.setAlignment(Pos.CENTER);
        playerPane.setHgap(1);
        playerPane.setVgap(1);
        playerPane.setPrefWrapLength(tileSize - 4);

        tilePane = new StackPane(tileBackground, infoLabel, playerPane);
        StackPane.setAlignment(infoLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(playerPane, Pos.CENTER);
        tilePane.setAlignment(Pos.CENTER);

        updateDisplay();
    }

    public StackPane getNode() {
        return tilePane;
    }

    public void setAsDoorCorridor(boolean isDoor) {
        this.isDoorCorridor = isDoor;
        updateDisplay();
    }

    @Override
    public void onTileChanged(Tile<GridPos> tile) {
        if (tile == this.tileModel) {
            updateDisplay();
        }
    }

    void updateDisplay() {
        playerPane.getChildren().clear();

        if (tileModel instanceof RoomTile roomTile) {
            tileBackground.setFill(Color.LIGHTSLATEGRAY); // Room color
            infoLabel.setText(roomTile.getRoomName());
            infoLabel.setVisible(true);
            infoLabel.setText("");
        } else if (tileModel instanceof CorridorTile) {
            if (this.isDoorCorridor) {
                tileBackground.setFill(Color.KHAKI);
            } else {
                tileBackground.setFill(Color.BEIGE);
            }
            infoLabel.setVisible(false);
        } else if (tileModel instanceof BorderTile) {
            tileBackground.setFill(Color.DARKSLATEGRAY); // Border color
            infoLabel.setVisible(false);
        }
        else {
            tileBackground.setFill(Color.LIGHTGRAY);
            infoLabel.setText("?");
            infoLabel.setVisible(true);
        }

        // Add player circles if any players are on this tile
        if (!tileModel.getPlayers().isEmpty()) {
            tileModel.getPlayers().forEach(player -> {
                Circle circle = new Circle(Math.max(3, tileSize * 0.1)); // Smaller player circle
                circle.setFill(PlayerColorMapper.toPaint(player.getColor()));
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(0.5);
                playerPane.getChildren().add(circle);
            });
        }
    }
}
