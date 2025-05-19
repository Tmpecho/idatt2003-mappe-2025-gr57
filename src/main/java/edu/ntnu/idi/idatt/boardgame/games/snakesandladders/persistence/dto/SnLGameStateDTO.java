package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto;

import edu.ntnu.idi.idatt.boardgame.core.persistence.dto.GameStateDTO;
import java.util.List;

/** JSON structure persisted on disk for Snakes & Ladders. */
public final class SnLGameStateDTO extends GameStateDTO {

  /** id of the player whose turn it is when the game is saved */
  public int currentPlayerTurn;

  public List<PlayerState> players;

  public static class PlayerState {
    public int id;
    public int position;
    public String color;
  }
}
