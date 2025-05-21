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
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Controller for the Snakes and Ladders game. Manages game flow, player turns, dice rolls, and game
 * state persistence.
 */
public final class SnlController extends GameController<LinearPos> {

  /**
   * Repository for saving and loading game state.
   */
  private final GameStateRepository<SnlGameStateDto> repo;

  private final int numberOfPlayers;
  /**
   * List of player colors to assign to players.
   */
  private final List<PlayerColor> playerColors = List.of(
      PlayerColor.RED,
      PlayerColor.BLUE,
      PlayerColor.GREEN,
      PlayerColor.YELLOW,
      PlayerColor.ORANGE,
      PlayerColor.PURPLE);

  /**
   * Constructs an SnLController.
   *
   * @param numberOfPlayers The number of players in the game.
   * @param repo            The {@link GameStateRepository} for handling persistence of
   *                        {@link SnlGameStateDto}.
   */
  public SnlController(
      int numberOfPlayers, GameStateRepository<SnlGameStateDto> repo) {
    super(new SnlBoard(), new Dice(2));
    this.numberOfPlayers = numberOfPlayers;
    this.repo = Objects.requireNonNull(repo);
    initialize(numberOfPlayers);
  }

  /**
   * Gets the map of players in the game.
   *
   * @return A map where keys are player IDs and values are {@link Player} objects.
   */
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

  @Override
  protected Map<Integer, Player<LinearPos>> createPlayers(int numberOfPlayers) {
    Map<Integer, Player<LinearPos>> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              PlayerColor color = playerColors.get((playerId - 1) % playerColors.size());
              Player<LinearPos> player = new Player<>(playerId, "Player " + playerId, color,
                  new LinearPos(1));
              players.put(playerId, player);
            });
    return players;
  }

  /**
   * Executes a dice roll for the current player, updates their position, checks for game over, and
   * advances to the next player if the game is not over.
   */
  public void rollDice() {
    Action roll = new RollAction((SnlBoard) gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(currentPlayer.getName() + " is now at tile " + currentPlayer.getPosition());
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
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  @Override
  public void saveGameState(String path) {
    try {
      repo.save(SnlMapper.toDto(this), Path.of(path));
      LoggingNotification.info("Game Saved", "Game state saved to " + path);
    } catch (Exception e) {
      System.err.println("Save failed: " + e.getMessage());
      LoggingNotification.error("Save failed", e.getMessage());
    }
  }

  @Override
  public void loadGameState(String path) {
    try {
      SnlMapper.apply(repo.load(Path.of(path)), this);
      notifyObservers("Game state loaded. Current turn: " + currentPlayer.getName());
    } catch (Exception e) {
      System.err.println("Load failed: " + e.getMessage());
      LoggingNotification.error("Load failed", e.getMessage());
    }
  }
}
