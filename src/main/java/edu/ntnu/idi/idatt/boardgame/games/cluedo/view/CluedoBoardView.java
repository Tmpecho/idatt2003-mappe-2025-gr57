package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.BorderTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public final class CluedoBoardView extends Pane implements TileObserver<LinearPos> {
  private static final int TILE_SIZE = 30;
  private static final int GAP_SIZE = 1;
  private static final int ROOM_CORNER_RADIUS = 10;
  private static final double ROOM_FONT_SCALE = 0.4;
  private static final int MIN_FONT_SIZE = 10;
  private static final int LABEL_PADDING = 5;
  private final GridPane grid;
  private final Map<GridPos, Node> tileMap = new HashMap<>();
  private Node highlightedNode = null;
  private final CluedoBoard boardModel;

  public CluedoBoardView(CluedoBoard boardModel) {
    this.boardModel = boardModel;
    this.grid = new GridPane();

    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);

    initializeBoard();
    getChildren().add(grid);
  }

  public void highlightTile(GridPos pos) {
    if (highlightedNode != null) {
      highlightedNode.setStyle("");
    }
    Node node = tileMap.get(pos);
    if (node != null) {
      node.setStyle("-fx-border-color: blue; -fx-border-width: 2; border-style: solid;");
      highlightedNode = node;
    }
  }

  private static FlowPane getFlowPane(StackPane roomPane) {
    FlowPane playerTokenPane = new FlowPane();
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
    return playerTokenPane;
  }

  private boolean isCorridorTileADoor(int corridorRow, int corridorCol) {
    int[] dr = {-1, 1, 0, 0};
    int[] dc = {0, 0, -1, 1};

    for (int i = 0; i < 4; i++) {
      int neighborRow = corridorRow + dr[i];
      int neighborCol = corridorCol + dc[i];

      // Check bounds for neighbor
      if (neighborRow >= 0
          && neighborRow < boardModel.getRows()
          && neighborCol >= 0
          && neighborCol < boardModel.getCols()) {

        AbstractCluedoTile neighborTile =
            boardModel.getTileAtPosition(new GridPos(neighborRow, neighborCol));
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
          addRoomTile(roomTile, numRows, numCols, boardGrid, visitedRoomCells);
        } else if (tileModel instanceof CorridorTile) {
          addCorridorTile(tileModel, row, col);
        } else if (tileModel instanceof BorderTile) {
          addBorderTile(tileModel, col, row);
        } else {
          addUnknownTile(col, row, tileModel);
        }
        }
    }
  }

  private void addUnknownTile(int col, int row, AbstractCluedoTile tileModel) {
    Pane unknownTilePane = new Pane();
    unknownTilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
    unknownTilePane.setStyle("-fx-background-color: #FF00FF;"); // Magenta for unknown
    Label unknownLabel = new Label("?");
    StackPane.setAlignment(unknownLabel, Pos.CENTER);
    StackPane wrapper = new StackPane(unknownTilePane, unknownLabel);
    grid.add(wrapper, col, row);
    System.err.println(
        "CluedoBoardView: Unhandled tile type at ("
            + row
            + ","
            + col
            + "): "
            + tileModel.getClass().getSimpleName());
  }

  private void addBorderTile(AbstractCluedoTile tileModel, int col, int row) {
    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
    Node node = tileView.getNode();
    grid.add(node, col, row);
    tileMap.put(new GridPos(row, col), node);

    bindClick(node, row, col);
  }

  private void addCorridorTile(AbstractCluedoTile tileModel, int row, int col) {
    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
    Node node = tileView.getNode();
    if (isCorridorTileADoor(row, col)) {
      tileView.setAsDoorCorridor(true);
    }
    grid.add(node, col, row);
    tileMap.put(new GridPos(row, col), node);
    bindClick(node, row, col);
  }

  private void addRoomTile(
      RoomTile roomTile,
      int numRows,
      int numCols,
      AbstractCluedoTile[][] boardGrid,
      boolean[][] visitedRoomCells) {
    RoomDimensions dimensions = calculateRoomDimensions(roomTile, numRows, numCols, boardGrid);
    StackPane roomPane = createRoomPane(roomTile, dimensions);

    grid.add(
        roomPane,
        dimensions.minCol(),
        dimensions.minRow(),
        dimensions.colSpan(),
        dimensions.rowSpan());

    // map every cell in that room to the same pane
    for (int r = dimensions.minRow(); r <= dimensions.maxRow(); r++) {
      for (int c = dimensions.minCol(); c <= dimensions.maxCol(); c++) {
        tileMap.put(new GridPos(r, c), roomPane);
      }
    }
    bindClick(roomPane, dimensions.minCol(), dimensions.minRow());

    markVisitedCells(dimensions, roomTile, boardGrid, visitedRoomCells);
    addPlayerTokensToRoomPane(roomPane, roomTile);
  }

  private RoomDimensions calculateRoomDimensions(
      RoomTile roomTile, int numRows, int numCols, AbstractCluedoTile[][] boardGrid) {
    int minRow = numRows, minCol = numCols, maxRow = -1, maxCol = -1;

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        if (boardGrid[row][col] == roomTile) {
          minRow = Math.min(minRow, row);
          maxRow = Math.max(maxRow, row);
          minCol = Math.min(minCol, col);
          maxCol = Math.max(maxCol, col);
        }
      }
    }
    return new RoomDimensions(minRow, maxRow, minCol, maxCol);
  }

  private StackPane createRoomPane(RoomTile roomTile, RoomDimensions dimensions) {
    Rectangle background = createRoomBackground(dimensions);
    Label label = createRoomLabel(roomTile.getRoomName());

    StackPane roomPane = new StackPane(background, label);
    roomPane.setAlignment(Pos.CENTER);
    return roomPane;
  }

  private Rectangle createRoomBackground(RoomDimensions dimensions) {
    Rectangle background =
        new Rectangle(
            dimensions.colSpan() * TILE_SIZE + (dimensions.colSpan() - 1) * GAP_SIZE,
            dimensions.rowSpan() * TILE_SIZE + (dimensions.rowSpan() - 1) * GAP_SIZE);
    background.setFill(Color.LIGHTSLATEGRAY);
    background.setStroke(Color.DARKGRAY);
    background.setArcWidth(ROOM_CORNER_RADIUS);
    background.setArcHeight(ROOM_CORNER_RADIUS);
    return background;
  }

  private Label createRoomLabel(String roomName) {
    Label label = new Label(roomName);
    label.setFont(Font.font("Arial", Math.max(MIN_FONT_SIZE, TILE_SIZE * ROOM_FONT_SCALE)));
    label.setTextAlignment(TextAlignment.CENTER);
    label.setTextFill(Color.WHITE);
    label.setPadding(new Insets(LABEL_PADDING));
    return label;
  }

  private void markVisitedCells(
      RoomDimensions dimensions,
      RoomTile roomTile,
      AbstractCluedoTile[][] boardGrid,
      boolean[][] visitedRoomCells) {
    for (int r = dimensions.minRow(); r <= dimensions.maxRow(); r++) {
      for (int c = dimensions.minCol(); c <= dimensions.maxCol(); c++) {
        if (boardGrid[r][c] == roomTile) {
          visitedRoomCells[r][c] = true;
        }
      }
    }
  }

  private void addPlayerTokensToRoomPane(StackPane roomPane, RoomTile roomTile) {
    if (!roomTile.getPlayers().isEmpty()) {
      FlowPane playerTokenPane = getFlowPane(roomPane);

      roomTile
          .getPlayers()
          .forEach(
              player -> {
                javafx.scene.shape.Circle circle =
                    new javafx.scene.shape.Circle(Math.max(5, TILE_SIZE * 0.25));
                circle.setFill(
                    edu.ntnu.idi.idatt.boardgame.ui.util.PlayerColorMapper.toPaint(
                        player.getColor()));
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

  @Override
  public void onTileChanged(Tile<LinearPos> tile) {
    System.out.println("Tile changed.");
  }

  private void bindClick(Node node, int row, int col) {
    node.setOnMouseClicked(e ->
        System.out.println("Pressed tile at [" + row + "," + col + "]")
    );
  }

  private record RoomDimensions(int minRow, int maxRow, int minCol, int maxCol) {
    public int rowSpan() {
      return maxRow - minRow + 1;
    }

    public int colSpan() {
      return maxCol - minCol + 1;
    }
  }
}
