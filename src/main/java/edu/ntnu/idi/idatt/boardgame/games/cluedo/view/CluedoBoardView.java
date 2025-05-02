package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
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

    private void initializeBoard() {
        int numRows = boardModel.getRows();
        int numCols = boardModel.getCols();
        AbstractCluedoTile[][] boardGrid = boardModel.getBoardGrid();
        boolean[][] visited = new boolean[numRows][numCols]; // Track visited cells

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (visited[row][col]) {
                    continue;
                }

                AbstractCluedoTile tileModel = boardGrid[row][col];

                if (tileModel instanceof RoomTile roomTile) {
                    int minR = row, minC = col, maxR = row, maxC = col;

                    for (int r = row; r < numRows; r++) {
                        for (int c = col; c < numCols; c++) {
                            if (boardGrid[r][c] == roomTile) {
                                if (r > maxR)
                                    maxR = r;
                                if (c > maxC)
                                    maxC = c;
                            } else if (c > col && boardGrid[r][c - 1] == roomTile) {
                                break;
                            }
                        }
                        if (r > row && boardGrid[r][col] != roomTile && boardGrid[r - 1][col] == roomTile) {
                            break;
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

                    grid.add(roomPane, col, row, colSpan, rowSpan);

                    for (int r = minR; r <= maxR; r++) {
                        for (int c = minC; c <= maxC; c++) {
                            if (r < numRows && c < numCols) {
                                visited[r][c] = true;
                            }
                        }
                    }

                    // TODO: A better approach would be a dedicated layer or pane for player tokens
                    // that can be positioned anywhere on the board, not tied to specific grid
                    // cells.
                    addPlayerTokensToPane(roomPane, roomTile);

                } else if (tileModel instanceof CorridorTile) {
                    // Handle corridor tiles as before
                    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
                    grid.add(tileView.getNode(), col, row);
                    visited[row][col] = true;
                } else {
                    Pane emptyPane = new Pane();
                    emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
                    grid.add(emptyPane, col, row);
                    visited[row][col] = true;
                }
            }
        }
    }

    private void addPlayerTokensToPane(StackPane pane, AbstractCluedoTile tileModel) {
        if (!tileModel.getPlayers().isEmpty()) {
            javafx.scene.layout.FlowPane playerTokenPane = new javafx.scene.layout.FlowPane();
            playerTokenPane.setAlignment(Pos.BOTTOM_CENTER);
            playerTokenPane.setHgap(2);
            playerTokenPane.setVgap(2);
            playerTokenPane.setPadding(new javafx.geometry.Insets(5));
            playerTokenPane.setMaxWidth(pane.getWidth() - 10);

            tileModel.getPlayers().forEach(player -> {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(Math.max(5, TILE_SIZE * 0.2));
                circle.setFill(edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper.toPaint(player.getColor()));
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
                playerTokenPane.getChildren().add(circle);
            });
            pane.getChildren().add(playerTokenPane);
        }
    }

    public void updateView() {
        grid.getChildren().clear();
        initializeBoard();
    }
}
