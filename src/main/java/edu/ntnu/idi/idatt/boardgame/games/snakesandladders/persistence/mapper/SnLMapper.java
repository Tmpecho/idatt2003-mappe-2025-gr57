package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller.SnLController;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnLGameStateDTO;
import java.util.ArrayList;
import java.util.List;

public final class SnLMapper {

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
      throw new IllegalStateException(
          "Save-file error: no player with id " + dto.currentPlayerTurn);
    }
    controller.setCurrentPlayer(current);
  }
}
