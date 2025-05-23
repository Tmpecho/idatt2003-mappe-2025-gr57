package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnlGameStateDto;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SnlControllerTest {

  private SnlController controller;
  private InMemorySnlGameStateRepository mockRepo;
  private List<PlayerSetupDetails> twoPlayerDetails;

  static class InMemorySnlGameStateRepository implements GameStateRepository<SnlGameStateDto> {

    SnlGameStateDto savedDto;
    Path savedPath;
    SnlGameStateDto dtoToLoad;

    @Override
    public void save(SnlGameStateDto dto, Path file) {
      this.savedDto = dto;
      this.savedPath = file;
    }

    @Override
    public SnlGameStateDto load(Path file) throws IOException {
      if (this.dtoToLoad == null) {
        throw new IOException("No DTO configured for loading at " + file);
      }
      return this.dtoToLoad;
    }

    public void setDtoToLoad(SnlGameStateDto dto) {
      this.dtoToLoad = dto;
    }
  }

  @BeforeEach
  void setUp() {
    mockRepo = new InMemorySnlGameStateRepository();
    twoPlayerDetails = List.of(
        new PlayerSetupDetails("Alice", Optional.of(PlayerColor.RED), Optional.empty()),
        new PlayerSetupDetails("Bob", Optional.of(PlayerColor.BLUE), Optional.empty())
    );
    controller = new SnlController(twoPlayerDetails, mockRepo);
  }

  @Test
  void constructor_initializesGameWithPlayers() {
    assertEquals(2, controller.getPlayers().size());
    assertNotNull(controller.getCurrentPlayer());
    assertEquals("Alice", controller.getCurrentPlayer().getName());
    assertEquals(1, controller.getCurrentPlayer().getPosition().index());
  }


  @Test
  void rollDice_updatesPlayerPositionAndAdvancesTurn() {
    Player<LinearPos> initialPlayer = controller.getCurrentPlayer();
    int initialPosition = initialPlayer.getPosition().index();

    controller.rollDice();

    Player<LinearPos> firstPlayerAfterRoll = controller.getPlayers().get(initialPlayer.getId());
    assertNotEquals(initialPosition, firstPlayerAfterRoll.getPosition().index(),
        "Player position should change after roll");

    assertNotEquals(initialPlayer.getId(), controller.getCurrentPlayer().getId(),
        "Turn should advance to the next player");
    assertEquals("Bob", controller.getCurrentPlayer().getName());
  }

  @Test
  void rollDice_multipleTimes_cyclesThroughPlayers() {
    assertEquals("Alice", controller.getCurrentPlayer().getName());
    controller.rollDice(); // Alice rolls, Bob's turn
    assertEquals("Bob", controller.getCurrentPlayer().getName());
    controller.rollDice(); // Bob rolls, Alice's turn
    assertEquals("Alice", controller.getCurrentPlayer().getName());
  }

  @Test
  void isGameOver_trueWhenPlayerReachesEnd() {
    Player<LinearPos> player = controller.getCurrentPlayer();
    controller.getGameBoard()
        .setPlayerPosition(player, new LinearPos(controller.getGameBoard().getBoardSize()));
    assertTrue(controller.isGameOver(), "Game should be over when player is at the last tile");
  }

  @Test
  void isGameOver_falseWhenPlayerNotAtEnd() {
    assertFalse(controller.isGameOver(), "Game should not be over at the start");
    controller.rollDice(); // Make a move
    assertFalse(controller.isGameOver(), "Game should not be over after one move (usually)");
  }

  @Test
  void getNextPlayer_cyclesCorrectly() {
    assertEquals(1, controller.getCurrentPlayer().getId()); // Alice
    Player<LinearPos> next = controller.getNextPlayer();
    assertEquals(2, next.getId()); // Bob
    controller.setCurrentPlayer(next);
    next = controller.getNextPlayer();
    assertEquals(1, next.getId()); // Alice
  }

  @Test
  void loadGameState_appliesDtoToController() throws IOException {
    SnlGameStateDto dto = new SnlGameStateDto();
    dto.currentPlayerTurn = 2;
    SnlGameStateDto.PlayerState ps1 = new SnlGameStateDto.PlayerState();
    ps1.id = 1;
    ps1.position = 15;
    ps1.color = "RED";
    SnlGameStateDto.PlayerState ps2 = new SnlGameStateDto.PlayerState();
    ps2.id = 2;
    ps2.position = 25;
    ps2.color = "BLUE";
    dto.players = List.of(ps1, ps2);

    mockRepo.setDtoToLoad(dto);

    // Create a new controller instance for loading to simulate clean load
    SnlController newController = new SnlController(null, mockRepo);
    newController.loadGameState("test_load.json");

    assertEquals(2, newController.getCurrentPlayer().getId());
    assertEquals("Player 2",
        newController.getCurrentPlayer().getName()); // Name derived from ID in this DTO
    assertEquals(2, newController.getPlayers().size());
    assertEquals(15, newController.getPlayers().get(1).getPosition().index());
    assertEquals(25, newController.getPlayers().get(2).getPosition().index());
  }

  @Test
  void playerInitializationFromDetails_setsCorrectNameAndColor() {
    Map<Integer, Player<LinearPos>> players = controller.getPlayers();
    Player<LinearPos> player1 = players.get(1);
    Player<LinearPos> player2 = players.get(2);

    assertNotNull(player1);
    assertEquals("Alice", player1.getName());
    assertEquals(PlayerColor.RED, player1.getColor());

    assertNotNull(player2);
    assertEquals("Bob", player2.getName());
    assertEquals(PlayerColor.BLUE, player2.getColor());
  }
}
