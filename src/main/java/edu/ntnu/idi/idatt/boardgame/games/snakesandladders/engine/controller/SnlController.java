package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnlBoard;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnlGameStateDto;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper.SnlMapper;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Snakes and Ladders game. Manages game flow, player turns, dice rolls, and game
 * state persistence.
 */
public final class SnlController extends GameController<LinearPos> {

  private static final Logger logger = LoggerFactory.getLogger(SnlController.class);

  /** Repository for saving and loading game state. */
  private final GameStateRepository<SnlGameStateDto> repo;

  private int actualNumberOfPlayers;

  /**
   * Constructs a SnlController with the specified player details and game state repository.
   *
   * @param playerDetailsList List of player setup details. Can be null/empty for loading.
   * @param repo Repository for saving and loading game state.
   */
  public SnlController(
      List<PlayerSetupDetails> playerDetailsList, GameStateRepository<SnlGameStateDto> repo) {
    super(new SnlBoard(), new Dice(2));
    this.repo = Objects.requireNonNull(repo);
    initializeGame(playerDetailsList);
  }

  @Override
  protected Map<Integer, Player<LinearPos>> setupPlayers(
      List<PlayerSetupDetails> playerDetailsList) {
    Map<Integer, Player<LinearPos>> newPlayersMap = new HashMap<>();

    if (playerDetailsList == null || playerDetailsList.isEmpty()) {
      this.actualNumberOfPlayers = 0;
      return newPlayersMap; // Return empty map
    }

    AtomicInteger playerIdCounter = new AtomicInteger(1);
    playerDetailsList.forEach(
        detail -> {
          int id = playerIdCounter.getAndIncrement();
          String name = detail.name();
          PlayerColor color =
              detail
                  .color()
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "Player color is missing for SnL player: " + name));
          Player<LinearPos> player = new Player<>(id, name, color, new LinearPos(1));
          newPlayersMap.put(id, player);
        });
    this.actualNumberOfPlayers = newPlayersMap.size(); // Set for new game
    return newPlayersMap;
  }

  public Map<Integer, Player<LinearPos>> getPlayers() {
    return players;
  }

  /**
   * Gets the player whose turn it is currently.
   *
   * @return The current {@link Player}.
   */
  public Player<LinearPos> getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Sets the current player. Used primarily when loading a game state.
   *
   * @param player The {@link Player} to set as current.
   */
  public void setCurrentPlayer(Player<LinearPos> player) {
    this.currentPlayer = player;
  }

  /** Rolls the dice for the current player and updates their position on the board. */
  public void rollDice() {
    Action roll = new RollAction((SnlBoard) gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(
        currentPlayer.getName()
            + " rolled "
            + (dice.getDie(0) + dice.getDie(1))
            + " and is now at tile "
            + currentPlayer.getPosition());
    if (isGameOver()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
      notifyObservers("Next turn: " + currentPlayer.getName());
    }
  }

  @Override
  protected boolean isGameOver() {
    return currentPlayer.getPosition().index() == gameBoard.getBoardSize();
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player<LinearPos> getNextPlayer() {
    if (players == null || players.isEmpty() || actualNumberOfPlayers == 0) {
      throw new IllegalStateException(
          "Cannot get next player, player map is empty or not initialized properly.");
    }
    int nextPlayerId = (currentPlayer.getId() % actualNumberOfPlayers) + 1;
    Player<LinearPos> nextPlayer = players.get(nextPlayerId);
    if (nextPlayer == null) {
      throw new IllegalStateException(
          "Next player ID "
              + nextPlayerId
              + " not found in players map. Current player: "
              + currentPlayer.getId()
              + ", actualNum: "
              + actualNumberOfPlayers);
    }
    return nextPlayer;
  }

  @Override
  public void saveGameState(String path) {
    try {
      repo.save(SnlMapper.toDto(this), Path.of(path));
      LoggingNotification.info("Game Saved", "Game state saved to " + path);
    } catch (Exception e) {
      logger.error("Save failed: {}", e.getMessage(), e);
      LoggingNotification.error("Save failed", e.getMessage());
    }
  }

  @Override
  public void loadGameState(String path) {
    try {
      SnlGameStateDto dto = repo.load(Path.of(path));
      // Important: Re-initialize players map based on DTO before applying positions
      Map<Integer, Player<LinearPos>> loadedPlayers = new HashMap<>();
      for (SnlGameStateDto.PlayerState ps : dto.players) {
        PlayerColor color = PlayerColor.valueOf(ps.color); // Assuming color string matches enum
        // Consider storing/loading actual player name from PlayerSetupDetails if it was saved
        Player<LinearPos> p =
            new Player<>(ps.id, "Player " + ps.id, color, new LinearPos(ps.position));
        loadedPlayers.put(ps.id, p);
      }
      this.players = loadedPlayers;
      this.actualNumberOfPlayers = this.players.size(); // Crucial for getNextPlayer
      // Apply positions to the now correctly populated this.players map
      SnlMapper.apply(dto, this); // This will set currentPlayer

      notifyObservers("Game state loaded. Current turn: " + currentPlayer.getName());
    } catch (Exception e) {
      logger.error("Load failed: {}", e.getMessage(), e);
      LoggingNotification.error("Load failed", e.getMessage());
      // Potentially reset game or go back to menu
    }
  }
}
