package edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.mapper;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.dto.CluedoGameStateDto;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for mapping the state of the Cluedo game between the `CluedoController` domain
 * object and the `CluedoGameStateDto` persistence structure.
 */
public final class CluedoMapper {
  private CluedoMapper() {}

  /** Snapshot the entire CluedoController state into a DTO. */
  public static CluedoGameStateDto toDto(CluedoController controller) {
    CluedoGameStateDto dto = new CluedoGameStateDto();
    dto.currentPlayerTurn = controller.getCurrentPlayer().getId();
    dto.phase = controller.getPhase();
    dto.stepsLeft = controller.getStepsLeft();

    List<CluedoGameStateDto.PlayerState> list = new ArrayList<>();
    controller
        .getPlayers()
        .values()
        .forEach(
            raw -> {
              CluedoPlayer player = (CluedoPlayer) raw;
              CluedoGameStateDto.PlayerState playerState = new CluedoGameStateDto.PlayerState();

              playerState.id = player.getId();
              playerState.row = player.getPosition().row();
              playerState.col = player.getPosition().col();
              playerState.colour = player.getColor().name();

              // build hand‐lists (by enum name)
              playerState.suspectHand = new ArrayList<>();
              Arrays.stream(Suspect.values())
                  .filter(player::hasCard)
                  .forEach(suspect -> playerState.suspectHand.add(suspect.name()));
              playerState.weaponHand = new ArrayList<>();
              Arrays.stream(Weapon.values())
                  .filter(player::hasCard)
                  .forEach(weapon -> playerState.weaponHand.add(weapon.name()));
              playerState.roomHand = new ArrayList<>();
              Arrays.stream(Room.values())
                  .filter(player::hasCard)
                  .forEach(room -> playerState.roomHand.add(room.name()));

              // build note‐maps
              playerState.suspectNotes = new HashMap<>();
              Arrays.stream(Suspect.values())
                  .forEach(
                      suspect ->
                          playerState.suspectNotes.put(
                              suspect.name(), player.isSuspectNoted(suspect)));
              playerState.weaponNotes = new HashMap<>();
              Arrays.stream(Weapon.values())
                  .forEach(
                      weapon ->
                          playerState.weaponNotes.put(weapon.name(), player.isWeaponNoted(weapon)));
              playerState.roomNotes = new HashMap<>();
              Arrays.stream(Room.values())
                  .forEach(
                      room -> playerState.roomNotes.put(room.name(), player.isRoomNoted(room)));

              list.add(playerState);
            });

    dto.players = list;
    return dto;
  }

  /** Apply a previously‐saved DTO back onto a fresh CluedoController. */
  public static void apply(CluedoGameStateDto dto, CluedoController controller) {
    dto.players.forEach(
        playerState -> {
          CluedoPlayer player = (CluedoPlayer) controller.getPlayers().get(playerState.id);
          if (player == null) {
            LoggingNotification.error("Load error", "No player with id " + playerState.id);
            throw new IllegalStateException("No player with id " + playerState.id);
          }

          // restore position
          controller
              .getGameBoard()
              .setPlayerPosition(player, new GridPos(playerState.row, playerState.col));

          // restore hand
          playerState.suspectHand.stream()
              .map(Suspect::valueOf)
              .filter(suspect -> !player.hasCard(suspect))
              .forEach(player::addCard);
          playerState.weaponHand.stream()
              .map(Weapon::valueOf)
              .filter(weapon -> !player.hasCard(weapon))
              .forEach(player::addCard);
          playerState.roomHand.stream()
              .map(Room::valueOf)
              .filter(room -> !player.hasCard(room))
              .forEach(player::addCard);

          // restore notes
          playerState.suspectNotes.forEach(
              (name, noted) -> {
                Suspect suspect = Suspect.valueOf(name);
                player.setSuspectNoted(suspect, noted);
              });
          playerState.weaponNotes.forEach(
              (name, noted) -> {
                Weapon weapon = Weapon.valueOf(name);
                player.setWeaponNoted(weapon, noted);
              });
          playerState.roomNotes.forEach(
              (name, noted) -> {
                Room room = Room.valueOf(name);
                player.setRoomNoted(room, noted);
              });
        });

    // restore whose turn it is
    CluedoPlayer current = (CluedoPlayer) controller.getPlayers().get(dto.currentPlayerTurn);
    if (current == null) {
      LoggingNotification.error("Load error", "No player with id " + dto.currentPlayerTurn);
      throw new IllegalStateException("No player with id " + dto.currentPlayerTurn);
    }
    controller.setPhase(dto.phase);
    controller.setStepsLeft(dto.stepsLeft);
    controller.setCurrentPlayer(current);
  }
}
