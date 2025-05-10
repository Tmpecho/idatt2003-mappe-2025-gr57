package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.BorderTile; // Import BorderTile
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
    private static final int GAP_SIZE = 0;
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

    private void initializeBoard() {
        grid.getChildren().clear(); // Clear previous children if re-initializing
        int numRows = boardModel.getRows();
        int numCols = boardModel.getCols();
        AbstractCluedoTile[][] boardGrid = boardModel.getBoardGrid();
        boolean[][] visited = new boolean[numRows][numCols]; // Track visited cells for room spanning

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (visited[row][col]) {
                    continue;
                }

                AbstractCluedoTile tileModel = boardGrid[row][col];

                if (tileModel == null) { // Handle potential null tiles (though CluedoBoard aims to fill all)
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    emptyPane.setStyle("-fx-background-color: #333;"); // Dark color for uninitialized
                    grid.add(emptyPane, col, row);
                    visited[row][col] = true;
                    continue;
                }


                if (tileModel instanceof RoomTile roomTile) {
                    // Logic for finding room span and drawing RoomTile
                    // (Assuming this part correctly identifies all cells belonging to the same RoomTile instance)
                    int minR = row, minC = col, maxR = row, maxC = col;

                    // Simple BFS/DFS or iteration needed here to correctly find all connected parts of THE SAME room instance
                    // The provided loop might not be robust for complex room shapes or if boardGrid[r][c] could be a *different* RoomTile
                    // For rectangular rooms defined as one instance, the existing logic might work if boardGrid[r][c] == roomTile is the check.

                    // To correctly find the span of a single RoomTile instance:
                    for(int rScan = 0; rScan < numRows; rScan++) {
                        for(int cScan = 0; cScan < numCols; cScan++) {
                            if (boardGrid[rScan][cScan] == roomTile) { // Check for the same instance
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
                    roomBackground.setArcWidth(5);
                    roomBackground.setArcHeight(5);

                    Label roomLabel = new Label(roomTile.getRoomName());
                    roomLabel.setFont(Font.font(Math.max(10, TILE_SIZE * 0.4)));
                    roomLabel.setTextAlignment(TextAlignment.CENTER);
                    roomLabel.setTextFill(Color.WHITE);

                    StackPane roomPane = new StackPane(roomBackground, roomLabel);
                    roomPane.setAlignment(Pos.CENTER);

                    grid.add(roomPane, minC, minR, colSpan, rowSpan); // Use minC, minR for grid placement

                    for (int r = minR; r <= maxR; r++) {
                        for (int c = minC; c <= maxC; c++) {
                            if (r < numRows && c < numCols && boardGrid[r][c] == roomTile) { // Mark all parts of this room instance as visited
                                visited[r][c] = true;
                            }
                        }
                    }
                    addPlayerTokensToPane(roomPane, roomTile);

                } else if (tileModel instanceof CorridorTile) {
                    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
                    grid.add(tileView.getNode(), col, row);
                    visited[row][col] = true;
                } else if (tileModel instanceof BorderTile) { // Handle BorderTile
                    Rectangle borderRect = new Rectangle(TILE_SIZE, TILE_SIZE);
                    borderRect.setFill(Color.DARKSLATEGRAY); // Color for the border
                    borderRect.setStroke(null);
                    StackPane borderPane = new StackPane(borderRect); // Use StackPane for consistency if needed later
                    grid.add(borderPane, col, row);
                    visited[row][col] = true;
                } else {
                    // Fallback for any other AbstractCluedoTile type not explicitly handled
                    Pane unknownTilePane = new Pane();
                    unknownTilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    unknownTilePane.setStyle("-fx-background-color: #FF00FF;"); // Magenta for unknown
                    Label unknownLabel = new Label("?");
                    StackPane.setAlignment(unknownLabel, Pos.CENTER);
                    StackPane wrapper = new StackPane(unknownTilePane, unknownLabel);
                    grid.add(wrapper, col, row);
                    visited[row][col] = true;
                    System.err.println("CluedoBoardView: Unhandled tile type at ("+row+","+col+"): " + tileModel.getClass().getSimpleName());
                }
            }
        }
    }

    private void addPlayerTokensToPane(StackPane pane, AbstractCluedoTile tileModel) {
        // This method adds tokens to a room's StackPane.
        // It might need adjustment if players can be on individual border/corridor tiles and need visual representation there too.
        // The CluedoTileView handles player tokens for CorridorTiles.
        if (!tileModel.getPlayers().isEmpty()) {
            javafx.scene.layout.FlowPane playerTokenPane = new javafx.scene.layout.FlowPane();
            playerTokenPane.setAlignment(Pos.BOTTOM_CENTER); // Or Pos.CENTER for rooms
            playerTokenPane.setHgap(2);
            playerTokenPane.setVgap(2);
            playerTokenPane.setPadding(new javafx.geometry.Insets(5));
            // Ensure playerTokenPane does not obscure room name if room is small
            playerTokenPane.setMaxWidth(Math.max(0, pane.getPrefWidth() - 10));
            playerTokenPane.setMaxHeight(Math.max(0, pane.getPrefHeight()/2 - 5));


            tileModel.getPlayers().forEach(player -> {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(Math.max(5, TILE_SIZE * 0.2));
                circle.setFill(edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper.toPaint(player.getColor()));
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
                playerTokenPane.getChildren().add(circle);
            });
            pane.getChildren().add(playerTokenPane);
            // Ensure it doesn't overlay label too much
            StackPane.setAlignment(playerTokenPane, Pos.BOTTOM_CENTER);

        }
    }

    public void updateView() {
        // The current implementation of initializeBoard clears and redraws everything.
        // For more dynamic updates (e.g., player movement only), a more targeted update would be better.
        // But for now, this is a full refresh.
        initializeBoard();
    }
}