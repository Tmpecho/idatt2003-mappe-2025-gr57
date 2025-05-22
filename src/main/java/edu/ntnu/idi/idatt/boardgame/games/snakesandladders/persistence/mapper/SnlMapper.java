package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnlController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnlGameStateDto;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class for converting between {@link SnlController} state and {@link SnlGameStateDto}. This
 * facilitates saving and loading the game state for Snakes and Ladders.
 */
public final class SnlMapper {

  /** Private constructor to prevent instantiation. */
  private SnlMapper() {
    // Utility class
  }

  /**
   * Converts the current state of an {@link SnlController} to an {@link SnlGameStateDto}.
   *
   * @param controller The game controller whose state is to be converted.
   * @return An {@link SnlGameStateDto} representing the controller's state.
   */
  public static SnlGameStateDto toDto(SnlController controller) {
    SnlGameStateDto dto = new SnlGameStateDto();
    dto.currentPlayerTurn = controller.getCurrentPlayer().getId();

    List<SnlGameStateDto.PlayerState> playerStates = new ArrayList<>();
    controller
        .getPlayers()
        .values()
        .forEach(
            player -> {
              var playerState = new SnlGameStateDto.PlayerState();
              playerState.id = player.getId();
              playerState.position = player.getPosition().index();
              playerState.color = player.getColor().name();
              playerStates.add(playerState);
            });
    dto.players = playerStates;
    return dto;
  }

  /**
   * Applies the state from an {@link SnlGameStateDto} to an {@link SnlController}. This method
   * modifies the controller to reflect the loaded game state. Note: This assumes the
   * SnLController's players map is already initialized with the correct number of players and IDs.
   *
   * @param dto The game state DTO to apply.
   * @param controller The game controller to update.
   * @throws IllegalStateException if a player ID from the DTO is not found in the controller.
   */
  public static void apply(SnlGameStateDto dto, SnlController controller) {
    dto.players.forEach(
        playerState -> {
          Player<LinearPos> player = controller.getPlayers().get(playerState.id);
          if (player != null) {
            controller
                .getGameBoard()
                .setPlayerPosition(player, new LinearPos(playerState.position));
          }
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
