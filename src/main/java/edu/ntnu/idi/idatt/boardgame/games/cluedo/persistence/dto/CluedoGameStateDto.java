package edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.dto;

import edu.ntnu.idi.idatt.boardgame.core.persistence.dto.GameStateDto;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.Phase;
import java.util.List;
import java.util.Map;

/**
 * JSON structure persisted on disk for Cluedo.
 */
public final class CluedoGameStateDto extends GameStateDto {

  /**
   * id of the player whose turn it is when the game is saved.
   */
  public int currentPlayerTurn;

  /**
   * Phase of the game when the game is saved.
   */
  public Phase phase;

  /**
   * Number of steps left for the current player when the game is saved.
   */
  public int stepsLeft;

  /**
   * List of player states.
   */
  public List<PlayerState> players;

  /**
   * Represents the persisted state of a single Cluedo player.
   */
  public static class PlayerState {

    /**
     * The player's unique ID (1–6).
     */
    public int id;

    /**
     * The player's position on the board (row index).
     */
    public int row;

    /**
     * The player's position on the board (col index).
     */
    public int col;

    /**
     * The player's colour, e.g. "RED", "WHITE".
     */
    public String colour;

    /**
     * The suspect cards in this player's hand, as their enum names.
     */
    public List<String> suspectHand;

    /**
     * The weapon cards in this player's hand, as their enum names.
     */
    public List<String> weaponHand;

    /**
     * The room cards in this player's hand, as their enum names.
     */
    public List<String> roomHand;

    /**
     * For each suspect, whether this player has it marked off in their notes. Key = Suspect.name(),
     * Value = true/false.
     */
    public Map<String, Boolean> suspectNotes;

    /**
     * Same, for weapons.
     */
    public Map<String, Boolean> weaponNotes;

    /**
     * Same, for rooms.
     */
    public Map<String, Boolean> roomNotes;
  }
}
