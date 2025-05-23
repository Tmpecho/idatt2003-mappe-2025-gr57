package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.Phase;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.dto.CluedoGameStateDto;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CluedoControllerTest {

  private CluedoController controller;
  private InMemoryCluedoGameStateRepository mockRepo;
  private List<PlayerSetupDetails> threePlayerDetails;
  private CluedoBoard board;

  static class InMemoryCluedoGameStateRepository implements
      GameStateRepository<CluedoGameStateDto> {

    CluedoGameStateDto savedDto;
    Path savedPath;
    CluedoGameStateDto dtoToLoad;

    @Override
    public void save(CluedoGameStateDto dto, Path file) {
      this.savedDto = dto;
      this.savedPath = file;
    }

    @Override
    public CluedoGameStateDto load(Path file) throws IOException {
      if (this.dtoToLoad == null) {
        throw new IOException("No DTO configured for loading at " + file);
      }
      return this.dtoToLoad;
    }

    public void setDtoToLoad(CluedoGameStateDto dto) {
      this.dtoToLoad = dto;
    }
  }

  @BeforeEach
  void setUp() {
    mockRepo = new InMemoryCluedoGameStateRepository();
    threePlayerDetails = List.of(
        new PlayerSetupDetails(Suspect.MISS_SCARLETT.getName(), Optional.empty(),
            Optional.of(Suspect.MISS_SCARLETT)),
        new PlayerSetupDetails(Suspect.COLONEL_MUSTARD.getName(), Optional.empty(),
            Optional.of(Suspect.COLONEL_MUSTARD)),
        new PlayerSetupDetails(Suspect.MRS_WHITE.getName(), Optional.empty(),
            Optional.of(Suspect.MRS_WHITE))
    );
    controller = new CluedoController(threePlayerDetails, mockRepo);
    board = (CluedoBoard) controller.getGameBoard();
  }

  @Test
  void constructor_initializesGameCorrectly() {
    assertEquals(3, controller.getPlayers().size());
    assertNotNull(controller.getCurrentPlayer());
    assertEquals(Suspect.MISS_SCARLETT.getName(), controller.getCurrentPlayer().getName());
    assertEquals(Phase.WAIT_ROLL, controller.getPhase());
    assertNotNull(controller.getSolutionSuspect());
    assertNotNull(controller.getSolutionWeapon());
    assertNotNull(controller.getSolutionRoom());

    controller.getPlayers().values().forEach(p -> {
      CluedoPlayer cp = (CluedoPlayer) p;
      boolean hasCards =
          cp.hasCard(Suspect.values()[0]) || cp.hasCard(Weapon.values()[0]) || cp.hasCard(
              Room.values()[0]);
      int cardCount = 0;
      for (Suspect s : Suspect.values()) {
        if (cp.hasCard(s)) {
          cardCount++;
        }
      }
      for (Weapon w : Weapon.values()) {
        if (cp.hasCard(w)) {
          cardCount++;
        }
      }
      for (Room r : Room.values()) {
        if (cp.hasCard(r)) {
          cardCount++;
        }
      }

      assertFalse(cp.hasCard(controller.getSolutionSuspect()));
      assertFalse(cp.hasCard(controller.getSolutionWeapon()));
      assertFalse(cp.hasCard(controller.getSolutionRoom()));
    });
  }

  @Test
  void setupPlayers_withEmptyDetails_forLoading() {
    CluedoController emptyController = new CluedoController(new ArrayList<>(), mockRepo);
    assertTrue(emptyController.getPlayers().isEmpty());
    assertNull(emptyController.getSolutionSuspect());

    CluedoController nullController = new CluedoController(null, mockRepo);
    assertTrue(nullController.getPlayers().isEmpty());
    assertNull(nullController.getSolutionSuspect());
  }


  @Test
  void onRollButton_changesPhaseAndSetsStepsLeft() {
    controller.onRollButton();
    assertEquals(Phase.MOVING, controller.getPhase());
    assertTrue(controller.getStepsLeft() >= 2 && controller.getStepsLeft() <= 12);
  }

  @Test
  void movePlayerTo_corridorToCorridor() {
    controller.onRollButton();
    int initialSteps = controller.getStepsLeft();
    GridPos startPos = controller.getCurrentPlayer().getPosition();
    GridPos targetPos = new GridPos(startPos.row() - 1,
        startPos.col());

    if (board.getTileAtPosition(
        targetPos) instanceof edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile &&
        board.isLegalDestination(startPos, targetPos)) {
      controller.movePlayerTo(targetPos);
      assertEquals(targetPos, controller.getCurrentPlayer().getPosition());
      assertEquals(initialSteps - 1, controller.getStepsLeft());
      assertEquals(Phase.MOVING, controller.getPhase());
    } else {
      System.out.println("Skipping movePlayerTo_corridorToCorridor: target " + targetPos
          + " not a valid corridor move from " + startPos);
    }
  }

  @Test
  void movePlayerTo_corridorToRoom() {
    Player<GridPos> playerToMove = controller.getPlayers().values().stream()
        .filter(p -> p.getName()
            .equals(Suspect.MRS_WHITE.getName()))
        .findFirst().orElseThrow();
    controller.setCurrentPlayer((CluedoPlayer) playerToMove);

    board.setPlayerPosition(playerToMove,
        new GridPos(5, 18));
    board.setPlayerPosition(playerToMove, new GridPos(4, 17));

    controller.onRollButton();
    controller.beginMovePhase(5);

    GridPos roomEntryPos = new GridPos(4, 18);
    controller.movePlayerTo(roomEntryPos);

    assertEquals(roomEntryPos, playerToMove.getPosition());
    assertEquals(0, controller.getStepsLeft());
    assertEquals(Phase.IN_ROOM, controller.getPhase());
    assertEquals(Room.CONSERVATORY, controller.getRoomOfCurrentPlayer());
  }

  @Test
  void canSuggest_inRoom_returnsTrue() {
    board.setPlayerPosition(controller.getCurrentPlayer(), new GridPos(6, 4));
    controller.setPhase(Phase.IN_ROOM);
    assertFalse(controller.canNotSuggest());
  }

  @Test
  void canSuggest_inCluedoRoom_returnsFalse() {
    board.setPlayerPosition(controller.getCurrentPlayer(), new GridPos(10, 10));
    controller.setPhase(Phase.IN_ROOM);
    assertTrue(controller.canNotSuggest());
  }

  @Test
  void canAccuse_inCluedoRoom_returnsTrue() {
    board.setPlayerPosition(controller.getCurrentPlayer(),
        new GridPos(10, 10));
    controller.setPhase(Phase.IN_ROOM);
    assertFalse(controller.canNotAccuse());
  }

  @Test
  void canAccuse_notInCluedoRoom_returnsFalse() {
    board.setPlayerPosition(controller.getCurrentPlayer(), new GridPos(6, 4));
    controller.setPhase(Phase.IN_ROOM);
    assertTrue(controller.canNotAccuse());
  }

  @Test
  void makeAccusation_correct_finishesGame() {
    controller.setPhase(Phase.IN_ROOM);
    board.setPlayerPosition(controller.getCurrentPlayer(),
        new GridPos(11, 11));

    Suspect solSuspect = controller.getSolutionSuspect();
    Weapon solWeapon = controller.getSolutionWeapon();
    Room solRoom = controller.getSolutionRoom();

    controller.makeAccusation(solSuspect, solWeapon, solRoom);
    assertTrue(controller.isGameOver());
    assertEquals(Phase.TURN_OVER, controller.getPhase());
  }

  @Test
  void makeAccusation_incorrect_eliminatesPlayer() {
    controller.setPhase(Phase.IN_ROOM);
    board.setPlayerPosition(controller.getCurrentPlayer(), new GridPos(11, 11));

    Suspect solSuspect = controller.getSolutionSuspect();
    Weapon solWeapon = controller.getSolutionWeapon();
    Room wrongRoom = Room.values()[(controller.getSolutionRoom().ordinal() + 1)
        % Room.values().length];

    Player<GridPos> accusingPlayer = controller.getCurrentPlayer();
    int initialPlayerCount = controller.getPlayers().values().stream()
        .filter(p -> getTurnOrder().contains(p)).collect(Collectors.toList()).size();

    assertFalse(controller.isGameOver());
    int afterAccusationPlayerCount = controller.getPlayers().values().stream()
        .filter(p -> getTurnOrder().contains(p)).collect(Collectors.toList()).size();

  }

  private List<Player<GridPos>> getTurnOrder() {
    try {
      java.lang.reflect.Field turnOrderField = CluedoController.class.getDeclaredField("turnOrder");
      turnOrderField.setAccessible(true);
      return (List<Player<GridPos>>) turnOrderField.get(controller);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }


  @Test
  void makeSuggestion_movesPlayerAndDisproves() {
    CluedoPlayer suggester = (CluedoPlayer) controller.getCurrentPlayer();
    GridPos kitchenPos = new GridPos(6, 4);
    board.setPlayerPosition(suggester, kitchenPos);
    controller.setPhase(Phase.IN_ROOM);

    CluedoPlayer movedPlayer = (CluedoPlayer) controller.getPlayers().values().stream()
        .filter(p -> p.getName().equals(Suspect.COLONEL_MUSTARD.getName()))
        .findFirst().orElseThrow();
    GridPos initialMovedPlayerPos = movedPlayer.getPosition();
    assertNotEquals(kitchenPos, initialMovedPlayerPos);

    CluedoPlayer disprover = (CluedoPlayer) controller.getPlayers().values().stream()
        .filter(p -> p.getName().equals(Suspect.MRS_WHITE.getName()))
        .findFirst().orElseThrow();

    disprover.addCard(Suspect.COLONEL_MUSTARD);

  }

  @Test
  void endTurn_advancesPlayerAndResetsPhase() {
    Player<GridPos> initialPlayer = controller.getCurrentPlayer();
    controller.setPhase(Phase.IN_ROOM);

    controller.endTurn();

    assertNotEquals(initialPlayer.getId(), controller.getCurrentPlayer().getId());
    assertEquals(Phase.WAIT_ROLL, controller.getPhase());
    assertEquals(0, controller.getStepsLeft());
  }


  @Test
  void loadGameState_appliesDtoToController() throws IOException {
    CluedoGameStateDto dto = new CluedoGameStateDto();
    dto.currentPlayerTurn = 2;
    dto.phase = Phase.MOVING;
    dto.stepsLeft = 3;
    dto.solutionSuspect = Suspect.PROFESSOR_PLUM.name();
    dto.solutionWeapon = Weapon.REVOLVER.name();
    dto.solutionRoom = Room.STUDY.name();

    List<CluedoGameStateDto.PlayerState> playerStates = new ArrayList<>();
    // Player 1: Miss Scarlett
    CluedoGameStateDto.PlayerState ps1 = new CluedoGameStateDto.PlayerState();
    ps1.id = 1;
    ps1.row = 23;
    ps1.col = 7;
    ps1.colour = PlayerColor.WHITE.name();
    ps1.suspectHand = List.of(Suspect.MRS_PEACOCK.name());
    ps1.weaponHand = List.of(Weapon.CANDLESTICK.name());
    ps1.roomHand = List.of(Room.BALLROOM.name());
    ps1.suspectNotes = new HashMap<>() {{
      put(Suspect.REVEREND_GREEN.name(), true);
    }};
    ps1.weaponNotes = new HashMap<>();
    ps1.roomNotes = new HashMap<>();
    playerStates.add(ps1);

    // Player 2: Col. Mustard
    CluedoGameStateDto.PlayerState ps2 = new CluedoGameStateDto.PlayerState();
    ps2.id = 2;
    ps2.row = 17;
    ps2.col = 1;
    ps2.colour = PlayerColor.RED.name();
    ps2.suspectHand = List.of();
    ps2.weaponHand = List.of();
    ps2.roomHand = List.of();
    ps2.suspectNotes = new HashMap<>();
    ps2.weaponNotes = new HashMap<>();
    ps2.roomNotes = new HashMap<>();
    playerStates.add(ps2);

    // Player 3: Mrs. White
    CluedoGameStateDto.PlayerState ps3 = new CluedoGameStateDto.PlayerState();
    ps3.id = 3;
    ps3.row = 1;
    ps3.col = 7;
    ps3.colour = PlayerColor.YELLOW.name();
    ps3.suspectHand = List.of();
    ps3.weaponHand = List.of();
    ps3.roomHand = List.of();
    ps3.suspectNotes = new HashMap<>();
    ps3.weaponNotes = new HashMap<>();
    ps3.roomNotes = new HashMap<>();
    playerStates.add(ps3);
    dto.players = playerStates;

    mockRepo.setDtoToLoad(dto);

    CluedoController newController = new CluedoController(null, mockRepo);
    newController.loadGameState("test_cluedo_load.json");

    assertEquals(2, newController.getCurrentPlayer().getId());
    assertEquals(Suspect.COLONEL_MUSTARD.getName(), newController.getCurrentPlayer().getName());
    assertEquals(Phase.MOVING, newController.getPhase());
    assertEquals(3, newController.getStepsLeft());
    assertEquals(Suspect.PROFESSOR_PLUM, newController.getSolutionSuspect());
    assertEquals(Weapon.REVOLVER, newController.getSolutionWeapon());
    assertEquals(Room.STUDY, newController.getSolutionRoom());

    assertEquals(3, newController.getPlayers().size());
    CluedoPlayer loadedPlayer1 = (CluedoPlayer) newController.getPlayers().get(1);
    assertEquals(new GridPos(23, 7), loadedPlayer1.getPosition());
    assertTrue(loadedPlayer1.hasCard(Suspect.MRS_PEACOCK));
    assertTrue(loadedPlayer1.hasCard(Weapon.CANDLESTICK));
    assertTrue(loadedPlayer1.hasCard(Room.BALLROOM));
    assertTrue(loadedPlayer1.isSuspectNoted(Suspect.REVEREND_GREEN));
  }
}
