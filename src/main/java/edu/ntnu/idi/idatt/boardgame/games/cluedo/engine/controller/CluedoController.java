package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class CluedoController extends GameController<GridPos> {

  private final int numberOfPlayers;
  private final List<PlayerColor> playerColors =
      List.of(
          PlayerColor.WHITE, // Miss Scarlett
          PlayerColor.RED, // Col. Mustard
          PlayerColor.BLUE, // Mrs. Peacock
          PlayerColor.GREEN, // Rev. Green
          PlayerColor.YELLOW, // Mrs. White
          PlayerColor.PURPLE // Prof. Plum
          );

  private final Map<PlayerColor, String> playerNames =
      Map.of(
          PlayerColor.WHITE, "Miss Scarlett",
          PlayerColor.RED, "Col. Mustard",
          PlayerColor.BLUE, "Mrs. Peacock",
          PlayerColor.GREEN, "Rev. Green",
          PlayerColor.YELLOW, "Mrs. White",
          PlayerColor.PURPLE, "Prof. Plum");
  private final CluedoBoard boardModel;
  private int stepsLeft = 0;

  public CluedoController(int numberOfPlayers) {
    super(new CluedoBoard(), new Dice(2));
    // grab a typed reference once:
    this.boardModel = (CluedoBoard) this.gameBoard;
    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }
    this.numberOfPlayers = numberOfPlayers;
    initialize(numberOfPlayers);
  }

  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int numPlayers) {
    Map<Integer, Player<GridPos>> playersMap = new HashMap<>();
    CluedoBoard board = (CluedoBoard) this.gameBoard;

    IntStream.range(0, numPlayers)
        .forEach(
            i -> {
              int playerId = i + 1;
              PlayerColor color = playerColors.get(i % playerColors.size());
              String name = playerNames.getOrDefault(color, "Player " + playerId);
              GridPos startPos = new GridPos(0, 0);
              Player<GridPos> player = new Player<>(playerId, name, color, startPos);
              playersMap.put(playerId, player);
            });
    return playersMap;
  }

  public Map<Integer, Player<GridPos>> getPlayers() {
    return players;
  }

  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
  }

  @Override
  protected boolean isGameOver() {
    // Game over condition: Correct accusation made or only one player left?
    // Placeholder: Game never ends for now
    return false;
  }

  @Override
  protected void onGameFinish() {
    // Actions when the game ends (e.g., declare winner)
    notifyGameFinished(currentPlayer); // Example notification
  }

  @Override
  protected Player<GridPos> getNextPlayer() {
    // Cycle through players based on ID
    int nextPlayerId = (currentPlayer.getId() % numberOfPlayers) + 1;
    return players.get(nextPlayerId);
  }

  @Override
  public void saveGameState(String filePath) {
    // TODO: Implement Cluedo-specific game state saving
    System.out.println("Cluedo save game state not implemented yet for path: " + filePath);
    // Need a CluedoGameStateDTO, Mapper, and Repository similar to SnL
  }

  @Override
  public void loadGameState(String filePath) {
    // TODO: Implement Cluedo-specific game state loading
    System.out.println("Cluedo load game state not implemented yet from path: " + filePath);
  }

  // --- Cluedo Specific Actions (Examples - Need Implementation) ---

  public void rollDiceAndMove() {
    int roll = dice.roll();

    this.stepsLeft = roll;

    notifyObservers(
        currentPlayer.getName()
            + " rolled a "
            + roll
            + ". Click an adjacent tile (or enter a room) to move. Steps remaining: "
            + stepsLeft);
  }

  public void movePlayerTo(GridPos target) {
    if (stepsLeft <= 0) {
      notifyObservers("No moves left. Roll the dice to start your turn.");
      return;
    }
    GridPos current = currentPlayer.getPosition();
    // 1-cell orthogonal:
    int dr = Math.abs(current.row() - target.row());
    int dc = Math.abs(current.col() - target.col());
    boolean orthogonalStep = (dr + dc == 1);

    // allow door-into-room moves:
    boolean doorEntry =
        boardModel.getTileAtPosition(current) instanceof CorridorTile
            && boardModel.getTileAtPosition(target) instanceof RoomTile
            && ((RoomTile) boardModel.getTileAtPosition(target))
                .canEnterFrom(current.row(), current.col());
    if (!orthogonalStep && !doorEntry) {
      notifyObservers("Invalid move: must move 1 space orthogonally or enter through a door.");
      return;
    }

    // update the model
    gameBoard.setPlayerPosition(currentPlayer, target);
    currentPlayer.setPosition(target);

    stepsLeft--;

    // notify the UI
    notifyObservers(
        currentPlayer.getName() + " moved to " + target + ". Steps remaining: " + stepsLeft);

    // if that was the last step, end turn
    if (stepsLeft == 0) {
      Player<GridPos> next = getNextPlayer();
      currentPlayer = next;
      notifyObservers("Turn over. It is now " + next.getName() + "'s turn.");
    }
  }

  public void makeSuggestion() {
    // TODO: Implement suggestion logic (only in rooms, move suspect/weapon)
    notifyObservers(currentPlayer.getName() + " suggestion logic TBD.");
  }

  public void makeAccusation() {
    // TODO: Implement accusation logic (check against solution, handle win/loss)
    notifyObservers(currentPlayer.getName() + " accusation logic TBD.");
    // if (isGameOver()) onGameFinish();
  }
}
