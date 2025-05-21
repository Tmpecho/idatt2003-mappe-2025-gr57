package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnLGameStateDTO;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class for converting between {@link SnLController} state and {@link SnLGameStateDTO}.
 * This facilitates saving and loading the game state for Snakes and Ladders.
 */
public final class SnLMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private SnLMapper() {
        // Utility class
    }

    /**
     * Converts the current state of an {@link SnLController} to an {@link SnLGameStateDTO}.
     *
     * @param controller The game controller whose state is to be converted.
     * @return An {@link SnLGameStateDTO} representing the controller's state.
     */
    public static SnLGameStateDTO toDto(SnLController controller) {
        SnLGameStateDTO dto = new SnLGameStateDTO();
        dto.currentPlayerTurn = controller.getCurrentPlayer().getId();

    List<SnLGameStateDTO.PlayerState> list = new ArrayList<>();
    controller
        .getPlayers()
        .values()
        .forEach(
            p -> {
              var ps = new SnLGameStateDTO.PlayerState();
              ps.id = p.getId();
              ps.position = p.getPosition().index();
              ps.color = p.getColor().name();
              list.add(ps);
            });
    dto.players = list;
    return dto;
  }

    /**
     * Applies the state from an {@link SnLGameStateDTO} to an {@link SnLController}.
     * This method modifies the controller to reflect the loaded game state.
     * Note: This assumes the SnLController's players map is already initialized
     * with the correct number of players and IDs.
     *
     * @param dto The game state DTO to apply.
     * @param controller The game controller to update.
     * @throws IllegalStateException if a player ID from the DTO is not found in the controller.
     */
  public static void apply(SnLGameStateDTO dto, SnLController controller) {
    dto.players.forEach(
        playerState -> {
          Player<LinearPos> p = controller.getPlayers().get(playerState.id);
          if (p != null)
            controller.getGameBoard().setPlayerPosition(p, new LinearPos(playerState.position));
        });

    // restore current turn
    Player<LinearPos> current = controller.getPlayers().get(dto.currentPlayerTurn);
    if (current == null) {
      LoggingNotification.error("Save-file error", "No player with id " + dto.currentPlayerTurn);
      throw new IllegalStateException(
          "Save-file error: no player with id " + dto.currentPlayerTurn);
    }
    controller.setCurrentPlayer(current);
  }
}
