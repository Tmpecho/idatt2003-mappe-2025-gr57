package edu.ntnu.idi.idatt.boardgame.games.cluedo.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.BorderTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import edu.ntnu.idi.idatt.boardgame.ui.util.PlayerTokenFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the visual view of the Cluedo game board. It arranges {@link CluedoTileView} instances
 * and handles room rendering. Implements {@link TileObserver} to react to changes in individual
 * tiles, particularly rooms.
 */
public final class CluedoBoardView extends Pane implements TileObserver<GridPos> {

  private static final Logger logger = LoggerFactory.getLogger(CluedoBoardView.class);

  private static final int TILE_SIZE = 30;
  private static final int GAP_SIZE = 1;
  private static final int ROOM_CORNER_RADIUS = 10;
  private static final double ROOM_FONT_SCALE = 0.4;
  private static final int MIN_FONT_SIZE = 10;
  private static final int LABEL_PADDING = 5;
  private final GridPane grid;
  private final Map<GridPos, Node> tileMap = new HashMap<>();
  private final Map<RoomTile, FlowPane> roomTokenPanes = new HashMap<>();
  private final Consumer<GridPos> onTileClick;
  private final CluedoBoard boardModel;
  private final Supplier<GridPos> currentPlayerPositionSupplier;
  private Node highlightedNode = null;

  /**
   * Constructs a CluedoBoardView.
   *
   * @param boardModel The {@link CluedoBoard} model this view represents.
   * @param currentPlayerPositionSupplier A supplier for the current player's position, used for
   *     click handling.
   * @param onTileClick A consumer that handles tile click events.
   */
  public CluedoBoardView(
      CluedoBoard boardModel,
      Supplier<GridPos> currentPlayerPositionSupplier,
      Consumer<GridPos> onTileClick) {
    this.boardModel = boardModel;
    this.onTileClick = onTileClick;
    this.currentPlayerPositionSupplier = currentPlayerPositionSupplier;
    this.grid = new GridPane();

    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);

    createFixedGridConstraints();

    initializeBoard();
    getChildren().add(grid);
  }

  private static FlowPane makePlayerPane() {
    FlowPane playerTokenPane = new FlowPane();
    playerTokenPane.setAlignment(Pos.CENTER);
    playerTokenPane.setHgap(1);
    playerTokenPane.setVgap(1);
    playerTokenPane.setPickOnBounds(false);
    return playerTokenPane;
  }

  /**
   * Highlights the tile at the given grid position. Any previously highlighted tile will be
   * un-highlighted.
   *
   * @param pos The {@link GridPos} of the tile to highlight.
   */
  public void highlightTile(GridPos pos) {
    if (highlightedNode != null) {
      highlightedNode.setStyle("");
    }
    Node node = tileMap.get(pos);
    if (node != null) {
      node.setStyle("-fx-border-color: blue;");
      highlightedNode = node;
    }
  }

  private boolean isCorridorTileDoor(int corridorRow, int corridorCol) {
    for (Direction direction : Direction.values()) {
      int neighborRow = corridorRow + direction.directionRow;
      int neighborCol = corridorCol + direction.directionCol;

      // Check bounds for neighbor
      if (neighborRow < 0
          || neighborRow >= boardModel.getBoardSize()
          || neighborCol < 0
          || neighborCol >= boardModel.getBoardSize()) {
        continue;
      }

      AbstractCluedoTile neighborTile =
          boardModel.getTileAtPosition(new GridPos(neighborRow, neighborCol));
      if (!(neighborTile instanceof RoomTile adjacentRoomTile)) {
        continue;
      }
      // Check if the adjacent room tile considers the corridor tile a door entry point
      if (adjacentRoomTile.canEnterFrom(corridorRow, corridorCol)) {
        return true;
      }
    }
    return false;
  }

  private void initializeBoard() {
    grid.getChildren().clear();
    int numRows = boardModel.getBoardSize();
    int numCols = boardModel.getBoardSize();

    AbstractCluedoTile[][] boardGrid = boardModel.getBoardGrid();
    boolean[][] visitedRoomCells = new boolean[numRows][numCols];

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        if (visitedRoomCells[row][col] && boardGrid[row][col] instanceof RoomTile) {
          continue;
        }

        AbstractCluedoTile tileModel = boardGrid[row][col];

        switch (tileModel) {
          case null -> {
            Pane emptyPane = new Pane();
            emptyPane.setPrefSize(TILE_SIZE, TILE_SIZE);
            emptyPane.setStyle("-fx-background-color: #1A1A1A;");
            grid.add(emptyPane, col, row);
          }
          case RoomTile roomTile ->
              addRoomTile(roomTile, numRows, numCols, boardGrid, visitedRoomCells);
          case CorridorTile corridorTile -> addCorridorTile(tileModel, row, col);
          case BorderTile borderTile -> addBorderTile(tileModel, col, row);
          default -> addUnknownTile(col, row, tileModel);
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
    LoggingNotification.error(
        "CluedoBoardView: Unhandled tile type",
        "Unhandled tile type at ("
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
    // Border tiles are generally not clickable for movement,
    // but binding for consistency or future use
    bindClick(node, row, col);
  }

  private void createFixedGridConstraints() {
    IntStream.range(0, boardModel.getBoardSize())
        .mapToObj(column -> new ColumnConstraints(TILE_SIZE))
        .forEach(
            columnConstraints -> {
              columnConstraints.setMinWidth(TILE_SIZE);
              columnConstraints.setPrefWidth(TILE_SIZE);
              columnConstraints.setMaxWidth(TILE_SIZE);
              columnConstraints.setHalignment(HPos.CENTER);
              grid.getColumnConstraints().add(columnConstraints);
            });
    IntStream.range(0, boardModel.getBoardSize())
        .mapToObj(row -> new RowConstraints(TILE_SIZE))
        .forEach(
            rowConstraints -> {
              rowConstraints.setMinHeight(TILE_SIZE);
              rowConstraints.setPrefHeight(TILE_SIZE);
              rowConstraints.setMaxHeight(TILE_SIZE);
              rowConstraints.setValignment(VPos.CENTER);
              grid.getRowConstraints().add(rowConstraints);
            });
  }

  private void addCorridorTile(AbstractCluedoTile tileModel, int row, int col) {
    CluedoTileView tileView = new CluedoTileView(tileModel, TILE_SIZE);
    Node node = tileView.getNode();
    if (isCorridorTileDoor(row, col)) {
      tileView.setAsDoorCorridor(true);
    }
    grid.add(node, col, row);
    tileMap.put(new GridPos(row, col), node);
    bindClick(node, row, col);
    tileModel.addObserver(this);
  }

  private void addRoomTile(
      RoomTile roomTile,
      int numRows,
      int numCols,
      AbstractCluedoTile[][] boardGrid,
      boolean[][] visitedRoomCells) {
    RoomDimensions dimensions = calculateRoomDimensions(roomTile, numRows, numCols, boardGrid);

    Rectangle background = createRoomBackground(dimensions);
    Label nameLabel = createRoomLabel(roomTile.getRoomName());

    FlowPane tokenPane = makePlayerPane();

    VBox content = new VBox(2, nameLabel, tokenPane);
    content.setAlignment(Pos.CENTER);

    StackPane roomPane = new StackPane(background, content);

    roomTokenPanes.put(roomTile, tokenPane);
    refreshRoomTokens(roomTile);

    grid.add(
        roomPane,
        dimensions.minCol(),
        dimensions.minRow(),
        dimensions.colSpan(),
        dimensions.rowSpan());

    // map every cell in that room to the same pane
    for (int minRow = dimensions.minRow(); minRow <= dimensions.maxRow(); minRow++) {
      for (int minCol = dimensions.minCol(); minCol <= dimensions.maxCol(); minCol++) {
        tileMap.put(new GridPos(minRow, minCol), roomPane);
      }
    }

    // Click handler for entering the room
    // A player must be on an adjacent corridor tile that is a door to this room.
    roomPane.setOnMouseClicked(
        e -> {
          GridPos here = currentPlayerPositionSupplier.get();
          for (Direction direction : Direction.values()) {
            GridPos candidate =
                new GridPos(
                    here.row() + direction.directionRow, here.col() + direction.directionCol);
            if (boardModel.getTileAtPosition(candidate) == roomTile) {
              onTileClick.accept(candidate);
              return;
            }
          }
          // silent ignore - this happens often
        });

    markVisitedCells(dimensions, roomTile, boardGrid, visitedRoomCells);
    roomTile.addObserver(this);
  }

  private RoomDimensions calculateRoomDimensions(
      RoomTile roomTile, int numRows, int numCols, AbstractCluedoTile[][] boardGrid) {
    int minRow = numRows;
    int minCol = numCols;
    int maxRow = -1;
    int maxCol = -1;

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
    for (int row = dimensions.minRow(); row <= dimensions.maxRow(); row++) {
      for (int col = dimensions.minCol(); col <= dimensions.maxCol(); col++) {
        if (boardGrid[row][col] == roomTile) {
          visitedRoomCells[row][col] = true;
        }
      }
    }
  }

  @Override
  public void onTileChanged(Tile<GridPos> tile) {
    if (tile instanceof RoomTile room) {
      Platform.runLater(() -> refreshRoomTokens(room));
    }
  }

  private void refreshRoomTokens(RoomTile room) {
    FlowPane pane = roomTokenPanes.get(room);
    if (pane == null) {
      // This can happen if the room wasn't fully initialized or is not in the map
      logger.warn("CluedoBoardView: No token pane found for room: {}", room.getRoomName());
      return;
    }

    pane.getChildren().clear();
    room.getPlayers()
        .forEach(
            player ->
                pane.getChildren()
                    .add(PlayerTokenFactory.createPlayerToken(player, TILE_SIZE, 0.10)));
  }

  private void bindClick(Node node, int row, int col) {
    GridPos pos = new GridPos(row, col);
    node.setOnMouseClicked(e -> onTileClick.accept(pos));
  }

  private enum Direction {
    NORTH(-1, 0),
    SOUTH(1, 0),
    WEST(0, -1),
    EAST(0, 1);
    public final int directionRow;
    public final int directionCol;

    Direction(int directionRow, int directionCol) {
      this.directionRow = directionRow;
      this.directionCol = directionCol;
    }
  }

  /**
   * Record to store the dimensions of a room on the grid.
   *
   * @param minRow The minimum row index of the room.
   * @param maxRow The maximum row index of the room.
   * @param minCol The minimum column index of the room.
   * @param maxCol The maximum column index of the room.
   */
  private record RoomDimensions(int minRow, int maxRow, int minCol, int maxCol) {

    /**
     * Returns the number of rows spanned by the room on the board grid.
     *
     * @return The span of rows occupied by the room.
     */
    public int rowSpan() {
      return maxRow - minRow + 1;
    }

    /**
     * Returns the number of columns spanned by the room on the board grid.
     *
     * @return The span of columns occupied by the room.
     */
    public int colSpan() {
      return maxCol - minCol + 1;
    }
  }
}
