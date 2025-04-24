package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper;

import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnLGameStateDTO;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class SnLMapper {

  public static SnLGameStateDTO toDto(SnLController controller) {
    SnLGameStateDTO dto = new SnLGameStateDTO();
    dto.currentPlayerTurn = controller.getCurrentPlayer().getId();

    List<SnLGameStateDTO.PlayerState> list = new ArrayList<>();
    controller.getPlayers().values().forEach(player -> {
      var playerState = new SnLGameStateDTO.PlayerState();
      playerState.id       = player.getId();
      playerState.position = player.getPosition();
      playerState.color    = player.getColor().name();
      list.add(playerState);
    });
    dto.players = list;
    return dto;
  }

  public static void apply(SnLGameStateDTO dto, SnLController controller) {
	  // restore board state
    dto.players.forEach(ps -> {
      Player player = controller.getPlayers().get(ps.id);
      if (player != null) {
        controller.getGameBoard().setPlayerPosition(player, ps.position);
      }
    });

	  // restore current turn
    Player current = controller.getPlayers().get(dto.currentPlayerTurn);
    if (current == null) {
      throw new IllegalStateException(
          "Save-file error: no player with id " + dto.currentPlayerTurn);
    }
    controller.setCurrentPlayer(current);
  }
}