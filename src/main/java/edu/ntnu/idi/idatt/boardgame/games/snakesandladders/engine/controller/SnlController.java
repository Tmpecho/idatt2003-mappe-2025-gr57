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

public final class SnlController extends GameController<LinearPos> {

  private final GameStateRepository<SnlGameStateDto> repo;
  private int actualNumberOfPlayers;

  private static final Logger logger = LoggerFactory.getLogger(SnlController.class);

  /**
   * Constructs an SnLController with custom player details.
   *
   * @param playerDetailsList List of player configurations.
   * @param repo              The repository for game state persistence.
   */
  public SnlController(
      List<PlayerSetupDetails> playerDetailsList, GameStateRepository<SnlGameStateDto> repo) {
    super(new SnlBoard(), new Dice(2));
    this.repo = Objects.requireNonNull(repo);
    this.actualNumberOfPlayers = playerDetailsList.size();
    initializeGame(playerDetailsList);
  }

  /**
   * Deprecated constructor. Use the constructor with PlayerSetupDetails.
   *
   * @param numberOfPlayers The number of players.
   * @param repo            The repository.
   */
  @Deprecated
  public SnlController(
      int numberOfPlayers, GameStateRepository<SnlGameStateDto> repo) {
    super(new SnlBoard(), new Dice(2));
    this.repo = Objects.requireNonNull(repo);
    this.actualNumberOfPlayers = numberOfPlayers; // For compatibility with old init path
    initialize(numberOfPlayers); // Calls old deprecated GameController.initialize
  }


  @Override
  protected Map<Integer, Player<LinearPos>> setupPlayers(
      List<PlayerSetupDetails> playerDetailsList) {
    Map<Integer, Player<LinearPos>> newPlayersMap = new HashMap<>();
    AtomicInteger playerIdCounter = new AtomicInteger(1); // For 1-based IDs

    playerDetailsList.forEach(detail -> {
      int id = playerIdCounter.getAndIncrement();
      String name = detail.name();
      PlayerColor color = detail.color().orElseThrow(() ->
          new IllegalArgumentException("Player color is missing for SnL player: " + name));
      // SnL players always start at LinearPos(1)
      Player<LinearPos> player = new Player<>(id, name, color, new LinearPos(1));
      newPlayersMap.put(id, player);
    });
    this.actualNumberOfPlayers = newPlayersMap.size(); // Update with actual count
    return newPlayersMap;
  }


  /**
   * @deprecated Use setupPlayers(List<PlayerSetupDetails>) with the new initialization flow.
   */
  @Deprecated
  @Override
  protected Map<Integer, Player<LinearPos>> createPlayers(int numberOfPlayers) {
    final List<PlayerColor> playerColors = List.of(
        PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN,
        PlayerColor.YELLOW, PlayerColor.ORANGE, PlayerColor.PURPLE);

    Map<Integer, Player<LinearPos>> tempPlayers = new HashMap<>();
    for (int i = 0; i < numberOfPlayers; i++) {
      int playerId = i + 1;
      PlayerColor color = playerColors.get(i % playerColors.size());
      Player<LinearPos> player = new Player<>(playerId, "Player " + playerId, color,
          new LinearPos(1));
      tempPlayers.put(playerId, player);
    }
    return tempPlayers;
  }

  public Map<Integer, Player<LinearPos>> getPlayers() {
    return players;
  }

  public Player<LinearPos> getCurrentPlayer() {
    return currentPlayer;
  }

  public void setCurrentPlayer(Player<LinearPos> player) {
    this.currentPlayer = player;
  }


  public void rollDice() {
    Action roll = new RollAction((SnlBoard) gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(currentPlayer.getName() + " rolled " + (dice.getDie(0) + dice.getDie(1))
        + " and is now at tile " + currentPlayer.getPosition());
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
    return players.get((currentPlayer.getId() % actualNumberOfPlayers) + 1);
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
      SnlMapper.apply(repo.load(Path.of(path)), this);
      this.actualNumberOfPlayers = this.players.size();
      notifyObservers("Game state loaded. Current turn: " + currentPlayer.getName());
    } catch (Exception e) {
      logger.error("Load failed: {}", e.getMessage(), e);
      LoggingNotification.error("Load failed", e.getMessage());
    }
  }
}
