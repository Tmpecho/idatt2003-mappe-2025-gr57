package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.domain.board;

import edu.ntnu.idi.idatt.boardgame.common.domain.board.Tile;
import edu.ntnu.idi.idatt.boardgame.common.domain.board.TileObserver;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnakesAndLaddersTile implements Tile {
  private final List<Player> players;
  private final int position;
  private final List<TileObserver> observers = new ArrayList<>();

  public SnakesAndLaddersTile(int position) {
    this.position = position;
    this.players = new ArrayList<>();
  }

  @Override
  public void addPlayer(Player player) {
    if (!players.contains(player)) {
      players.add(player);
      notifyObservers();
    }
  }

  @Override
  public void removePlayer(Player player) {
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

  public List<Player> getPlayers() {
    return Collections.unmodifiableList(players);
  }
}
