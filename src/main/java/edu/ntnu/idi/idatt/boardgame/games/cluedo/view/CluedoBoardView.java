package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.BorderTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class CluedoBoardView extends Pane {
    private static final int TILE_SIZE = 30;
    private static final int GAP_SIZE = 1;
    private final GridPane grid;
    private final CluedoBoard boardModel;

    public CluedoBoardView(CluedoBoard boardModel) {
        this.boardModel = boardModel;
        this.grid = new GridPane();

        grid.setHgap(GAP_SIZE);
        grid.setVgap(GAP_SIZE);

        initializeBoard();
        getChildren().add(grid);
    }

    private boolean isCorridorTileADoor(int corridorRow, int corridorCol) {
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int neighborRow = corridorRow + dr[i];
            int neighborCol = corridorCol + dc[i];

            // Check bounds for neighbor
            if (neighborRow >= 0 && neighborRow < boardModel.getRows() &&
                    neighborCol >= 0 && neighborCol < boardModel.getCols()) {

                AbstractCluedoTile neighborTile = boardModel.getTileAtPosition(new GridPos(neighborRow, neighborCol));
                if (neighborTile instanceof RoomTile adjacentRoomTile) {
                    if (adjacentRoomTile.canEnterFrom(corridorRow, corridorCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initializeBoard() {
        grid.getChildren().clear();
        int numRows = boardModel.getRows();
        int numCols = boardModel.getCols();
        AbstractCluedoTile[][] boardGrid = boardModel.getBoardGrid();
        boolean[][] visitedRoomCells = new boolean[numRows][numCols];

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (visitedRoomCells[row][col] && boardGrid[row][col] instanceof RoomTile) {
                    continue;
                }

                AbstractCluedoTile tileModel = boardGrid[row][col];

                if (tileModel == null) {
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    emptyPane.setStyle("-fx-background-color: #1A1A1A;");
                    grid.add(emptyPane, col, row);
                    continue;
                }

                if (tileModel instanceof RoomTile roomTile) {
                    int minR = numRows, minC = numCols, maxR = -1, maxC = -1;
                    for (int rScan = 0; rScan < numRows; rScan++) {
                        for (int cScan = 0; cScan < numCols; cScan++) {
                            if (boardGrid[rScan][cScan] == roomTile) {
                                minR = Math.min(minR, rScan);
                                maxR = Math.max(maxR, rScan);
                                minC = Math.min(minC, cScan);
                                maxC = Math.max(maxC, cScan);
                            }
                        }
                    }

                    int rowSpan = maxR - minR + 1;
                    int colSpan = maxC - minC + 1;

                    Rectangle roomBackground = new Rectangle(colSpan * TILE_SIZE + (colSpan - 1) * GAP_SIZE,
                            rowSpan * TILE_SIZE + (rowSpan - 1) * GAP_SIZE);
                    roomBackground.setFill(Color.LIGHTSLATEGRAY);
                    roomBackground.setStroke(Color.DARKGRAY);
                    roomBackground.setArcWidth(10);
                    roomBackground.setArcHeight(10);

                    Label roomLabel = new Label(roomTile.getRoomName());
                    roomLabel.setFont(Font.font("Arial", Math.max(10, TILE_SIZE * 0.4)));
                    roomLabel.setTextAlignment(TextAlignment.CENTER);
                    roomLabel.setTextFill(Color.WHITE);
                    roomLabel.setPadding(new javafx.geometry.Insets(5));

                    StackPane roomPane = new StackPane(roomBackground, roomLabel);
                    roomPane.setAlignment(Pos.CENTER);

                    grid.add(roomPane, minC, minR, colSpan, rowSpan);

                    for (int r = minR; r <= maxR; r++) {
                        for (int c = minC; c <= maxC; c++) {
                            if (boardGrid[r][c] == roomTile) {
                                visitedRoomCells[r][c] = true;
                            }
                        }
                    }
                    addPlayerTokensToRoomPane(roomPane, roomTile);

                } else if (tileModel instanceof CorridorTile) {
                    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
                    if (isCorridorTileADoor(row, col)) {
                        tileView.setAsDoorCorridor(true);
                    }
                    grid.add(tileView.getNode(), col, row);
                } else if (tileModel instanceof BorderTile) {
                    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
                    grid.add(tileView.getNode(), col, row);
                } else {
                    Pane unknownTilePane = new Pane();
                    unknownTilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    unknownTilePane.setStyle("-fx-background-color: #FF00FF;"); // Magenta for unknown
                    Label unknownLabel = new Label("?");
                    StackPane.setAlignment(unknownLabel, Pos.CENTER);
                    StackPane wrapper = new StackPane(unknownTilePane, unknownLabel);
                    grid.add(wrapper, col, row);
                    System.err.println(
                            "CluedoBoardView: Unhandled tile type at ("+row+","+col+"): "
                                    + tileModel.getClass().getSimpleName());
                }
            }
        }
    }

    private void addPlayerTokensToRoomPane(StackPane roomPane, RoomTile roomTile) {
        if (!roomTile.getPlayers().isEmpty()) {
            javafx.scene.layout.FlowPane playerTokenPane = new javafx.scene.layout.FlowPane();
            playerTokenPane.setAlignment(Pos.BOTTOM_CENTER);
            playerTokenPane.setHgap(3);
            playerTokenPane.setVgap(3);
            playerTokenPane.setPadding(new javafx.geometry.Insets(5));
            // Make token pane pick up mouse events if needed later, but not block room label
            playerTokenPane.setPickOnBounds(false);


            double roomPaneWidth = roomPane.getWidth();
            if (roomPaneWidth <= 0) roomPaneWidth = TILE_SIZE * 2;
            double roomPaneHeight = roomPane.getHeight();
            if (roomPaneHeight <= 0) roomPaneHeight = TILE_SIZE * 2;

            playerTokenPane.setMaxWidth(Math.max(TILE_SIZE, roomPaneWidth - 20));
            playerTokenPane.setMaxHeight(Math.max(TILE_SIZE / 2.0, roomPaneHeight / 3.0));


            roomTile.getPlayers().forEach(player -> {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(Math.max(5, TILE_SIZE * 0.25));
                circle.setFill(edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper.toPaint(player.getColor()));
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1.5);
                playerTokenPane.getChildren().add(circle);
            });

            roomPane.getChildren().add(playerTokenPane);
            StackPane.setAlignment(playerTokenPane, Pos.BOTTOM_CENTER);
        }
    }


    public void updateView() {
        // This full refresh can be slow for large boards or frequent updates.
        // For player movement, only updating affected tiles would be more efficient.
        initializeBoard();
    }
}
