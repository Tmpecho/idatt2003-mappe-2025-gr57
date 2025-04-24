package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import java.util.ArrayList;
import java.util.List;

/** Grid-based tile with (row,col) coordinates and default observer support. */
public abstract class AbstractCluedoTile implements Tile {

  protected final int row; // 0-based board coordinates
  protected final int col;
  protected final List<Player> players = new ArrayList<>();

  private final List<TileObserver> observers = new ArrayList<>();

  protected AbstractCluedoTile(int row, int col) {
    this.row = row;
    this.col = col;
  }

  @Override
  public void addPlayer(Player player) {
    players.add(player);
    notifyChange();
  }

  @Override
  public void removePlayer(Player player) {
    players.remove(player);
    notifyChange();
  }

  @Override
  public String getIdentifier() {
    return "%s(%d,%d)".formatted(getClass().getSimpleName(), row, col);
  }

  @Override
  public void addObserver(TileObserver obs) {
    observers.add(obs);
  }

  @Override
  public void removeObserver(TileObserver obs) {
    observers.remove(obs);
  }

  protected void notifyChange() {
    observers.forEach(o -> o.onTileChanged(this));
  }

  public int row() {
    return row;
  }

  public int col() {
    return col;
  }

  public List<Player> getPlayers() {
    return List.copyOf(players);
  }
}
