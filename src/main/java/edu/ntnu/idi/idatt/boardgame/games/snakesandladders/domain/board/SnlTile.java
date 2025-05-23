package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single tile on the Snakes and Ladders board. It holds a list of players currently on
 * this tile and its position number.
 */
public final class SnlTile implements Tile<LinearPos> {

  /** List of players currently occupying this tile. */
  private final List<Player<LinearPos>> players;

  /** The 1-based position number of this tile on the board. */
  private final int position;

  /** List of observers monitoring this tile. */
  private final List<TileObserver<LinearPos>> observers = new ArrayList<>();

  /**
   * Constructs an SnLTile with a given position number.
   *
   * @param position The 1-based position of this tile on the board.
   */
  public SnlTile(int position) {
    this.position = position;
    this.players = new ArrayList<>();
  }

  @Override
  public void addPlayer(Player<LinearPos> player) {
    if (!players.contains(player)) {
      players.add(player);
      notifyObservers();
    }
  }

  @Override
  public void removePlayer(Player<LinearPos> player) {
    if (players.remove(player)) {
      notifyObservers();
    }
  }

  @Override
  public String getIdentifier() {
    return "Tile #" + position;
  }

  @Override
  public void addObserver(TileObserver<LinearPos> observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  @Override
  public void removeObserver(TileObserver<LinearPos> observer) {
    observers.remove(observer);
  }

  /** Notifies all registered observers that this tile has changed. */
  private void notifyObservers() {
    observers.forEach(observer -> observer.onTileChanged(this));
  }

  /**
   * Gets the 1-based position number of this tile.
   *
   * @return The position number.
   */
  public int getPosition() {
    return position;
  }

  /**
   * Gets an unmodifiable list of players currently on this tile.
   *
   * @return An unmodifiable list of players.
   */
  public List<Player<LinearPos>> getPlayers() {
    return Collections.unmodifiableList(players);
  }
}
