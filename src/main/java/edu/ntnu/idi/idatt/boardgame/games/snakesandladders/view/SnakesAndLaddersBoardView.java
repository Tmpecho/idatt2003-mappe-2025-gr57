package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.view;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.Connector;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnakesAndLaddersBoard;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class SnakesAndLaddersBoardView extends Pane implements TileObserver {
  private static final int TILE_SIZE = 60;
  private static final int GAP_SIZE = 5;
  private final GridPane grid;
  private final Group connectorGroup;
  private final SnakesAndLaddersBoard boardModel;

  public SnakesAndLaddersBoardView(SnakesAndLaddersBoard boardModel) {
    this.boardModel = boardModel;
    this.grid = new GridPane();
    this.connectorGroup = new Group();

    grid.setHgap(GAP_SIZE);
    grid.setVgap(GAP_SIZE);

    initializeBoard();
    getChildren().addAll(grid, connectorGroup);
  }

  private void initializeBoard() {
    boardModel
        .getTiles()
        .forEach(
            (pos, tile) -> {
              int[] gridPos = getGridCoordinates(pos);
              SnakesAndLaddersTileView tileView = new SnakesAndLaddersTileView(tile, TILE_SIZE);
              grid.add(tileView.getNode(), gridPos[0], gridPos[1]);
              // Register this board view as an observer of the tile
              tile.addObserver(this);
            });

    boardModel.getConnectors().forEach(this::drawConnector);
  }

  private int[] getGridCoordinates(int pos) {
    int index = pos - 1;
    int rowFromBottom = index / boardModel.getCols();
    int col;

    if (rowFromBottom % 2 == 0) {
      col = index % boardModel.getCols();
    } else {
      col = boardModel.getCols() - 1 - (index % boardModel.getCols());
    }
    int gridRow = boardModel.getRows() - 1 - rowFromBottom;
    return new int[] {col, gridRow};
  }

  private void drawConnector(Connector connector) {
    double[] startCenter = getTileCenter(connector.getStart());
    double[] endCenter = getTileCenter(connector.getEnd());
    Line line = new Line(startCenter[0], startCenter[1], endCenter[0], endCenter[1]);
    line.setStroke(connector.getColor());
    line.setStrokeWidth(3);
    connectorGroup.getChildren().add(line);
  }

  private double[] getTileCenter(int pos) {
    int[] coords = getGridCoordinates(pos);
    double x = coords[0] * (TILE_SIZE + GAP_SIZE) + TILE_SIZE / 2.0;
    double y = coords[1] * (TILE_SIZE + GAP_SIZE) + TILE_SIZE / 2.0;
    return new double[] {x, y};
  }

  @Override
  public void onTileChanged(Tile tile) {
  }
}
