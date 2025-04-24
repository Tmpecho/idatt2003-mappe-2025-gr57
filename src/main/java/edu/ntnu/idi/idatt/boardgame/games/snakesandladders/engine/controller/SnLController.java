package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnLBoard;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.dto.SnLGameStateDTO;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.persistence.mapper.SnLMapper;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class SnLController extends GameController {
  private final GameStateRepository<SnLGameStateDTO> repo;

  private final int numberOfPlayers;
  private final List<PlayerColor> playerColors =
      List.of(
          PlayerColor.RED,
          PlayerColor.BLUE,
          PlayerColor.GREEN,
          PlayerColor.YELLOW,
          PlayerColor.ORANGE,
          PlayerColor.PURPLE);

  public SnLController(
      int numberOfPlayers, GameStateRepository<SnLGameStateDTO> repo) {
    super(new SnLBoard(), new Dice(2));
    this.numberOfPlayers = numberOfPlayers;
    this.repo = Objects.requireNonNull(repo);
    initialize(numberOfPlayers);
  }

  public Map<Integer, Player> getPlayers() {
    return players;
  }

  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  public void setCurrentPlayer(Player player) {
    this.currentPlayer = player;
  }

  @Override
  protected Map<Integer, Player> createPlayers(int numberOfPlayers) {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              PlayerColor color = playerColors.get((playerId - 1) % playerColors.size());
              Player player = new Player(playerId, "Player " + playerId, color);
              players.put(playerId, player);
            });
    return players;
  }

  public void rollDice() {
    Action roll = new RollAction((SnLBoard) gameBoard, currentPlayer, dice);
    roll.execute();
    notifyObservers(currentPlayer.getName() + " is now at tile " + currentPlayer.getPosition());
    if (isGameOver()) {
      onGameFinish();
    } else {
      currentPlayer = getNextPlayer();
    }
  }

  @Override
  protected boolean isGameOver() {
    return currentPlayer.getPosition() == gameBoard.getBoardSize();
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  @Override
  public void saveGameState(String path) {
    try {
      repo.save(SnLMapper.toDto(this), Path.of(path));
    } catch (Exception e) {
      System.err.println("Save failed: " + e.getMessage());
    }
  }

  @Override
  public void loadGameState(String path) {
    try {
      SnLMapper.apply(repo.load(Path.of(path)), this);
      notifyObservers("Game state loaded. Current turn: " + currentPlayer.getName());
    } catch (Exception e) {
      System.err.println("Load failed: " + e.getMessage());
    }
  }
}
