package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.event.TileObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnLTile implements Tile<LinearPos> {
  private final List<Player<LinearPos>> players;
  private final int position;
  private final List<TileObserver> observers = new ArrayList<>();

  public SnLTile(int position) {
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
  public void addObserver(TileObserver observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  @Override
  public void removeObserver(TileObserver observer) {
    observers.remove(observer);
  }

  private void notifyObservers() {
    observers.forEach(observer -> observer.onTileChanged(this));
  }

  public int getPosition() {
    return position;
  }

  public List<Player<LinearPos>> getPlayers() {
    return Collections.unmodifiableList(players);
  }
}
