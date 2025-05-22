package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto;

import edu.ntnu.idi.idatt.boardgame.core.persistence.dto.GameStateDto;
import java.util.List;

/**
 * JSON structure persisted on disk for Snakes & Ladders.
 */
public final class SnlGameStateDto extends GameStateDto {

  /**
   * id of the player whose turn it is when the game is saved.
   */
  public int currentPlayerTurn;

  /**
   * List of player states.
   */
  public List<PlayerState> players;

  /**
   * Represents the persisted state of a single player.
   */
  public static class PlayerState {

    /**
     * The ID of the player.
     */
    public int id;
    /**
     * The 1-based position of the player on the board.
     */
    public int position;
    /**
     * The string representation of the player's color (e.g., "RED").
     */
    public String color;
  }
}
