package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * Grid-based tile with (row,col) coordinates and default observer support.
 */
public abstract class AbstractCluedoTile implements Tile<GridPos> {

  /**
   * The 0-based row coordinate of this tile on the board.
   */
  protected final int row;
  /**
   * The 0-based column coordinate of this tile on the board.
   */
  protected final int col;
  /**
   * List of players currently on this tile.
   */
  protected final List<Player<GridPos>> players = new ArrayList<>();

  /**
   * Whether this tile is considered walkable (e.g., a corridor). Rooms might be !walkable.
   */
  protected boolean walkable = true;

  private final List<TileObserver<GridPos>> observers = new ArrayList<>(); // Use generic type

  /**
   * Constructs an AbstractCluedoTile at the given row and column.
   *
   * @param row The row coordinate.
   * @param col The column coordinate.
   */
  protected AbstractCluedoTile(int row, int col) {
    this.row = row;
    this.col = col;
  }

  @Override
  public void addPlayer(Player<GridPos> player) {
    players.add(player);
    notifyChange();
  }

  @Override
  public void removePlayer(Player<GridPos> player) {
    if (players.remove(player)) {
      notifyChange();
    }
  }

  @Override
  public String getIdentifier() {
    return "%s(%d,%d)".formatted(getClass().getSimpleName(), row, col);
  }

  @Override
  public void addObserver(TileObserver<GridPos> obs) { // Use generic type
    if (!observers.contains(obs)) {
      observers.add(obs);
    }
  }

  @Override
  public void removeObserver(TileObserver<GridPos> obs) { // Use generic type
    observers.remove(obs);
  }

  /**
   * Notifies all registered observers that this tile has changed.
   */
  protected void notifyChange() {
    List<TileObserver<GridPos>> observersCopy = new ArrayList<>(observers);
    observersCopy.forEach(o -> o.onTileChanged(this));
  }

  /**
   * Gets the row coordinate of this tile.
   *
   * @return The row coordinate.
   */
  public int row() {
    return row;
  }

  /**
   * Gets the column coordinate of this tile.
   *
   * @return The column coordinate.
   */
  public int col() {
    return col;
  }

  /**
   * Gets an unmodifiable list of players currently on this tile.
   *
   * @return A list of players.
   */
  public List<Player<GridPos>> getPlayers() {
    return List.copyOf(players);
  }

  /**
   * Sets whether this tile is walkable.
   *
   * @param value True if walkable, false otherwise.
   */
  public void setWalkable(boolean value) {
    walkable = value;
  }

  /**
   * Checks if this tile is walkable.
   *
   * @return True if walkable, false otherwise.
   */
  public boolean isWalkable() {
    return walkable;
  }
}
