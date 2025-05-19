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
  private final CluedoBoard boardModel;
  private final int numberOfPlayers;
  private int stepsLeft = 0;

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

  public CluedoController(int numberOfPlayers) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;
    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }
    this.numberOfPlayers = numberOfPlayers;
    initialize(numberOfPlayers);
  }

  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int numPlayers) {
    Map<Integer, Player<GridPos>> map = new HashMap<>();
    IntStream.range(0, numPlayers)
        .forEach(
            i -> {
              int id = i + 1;
              PlayerColor color = playerColors.get(i % playerColors.size());
              String name = playerNames.getOrDefault(color, "Player " + id);
              // you should pick valid start cells; using (0,0) as placeholder
              GridPos start = new GridPos(0, 0);
              map.put(id, new Player<>(id, name, color, start));
            });
    return map;
  }

  @Override
  public boolean isGameOver() {
    return false;
  }

  @Override
  protected void onGameFinish() {
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player<GridPos> getNextPlayer() {
    int nextId = (currentPlayer.getId() % numberOfPlayers) + 1;
    return players.get(nextId);
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

  /** How many steps remain this turn. */
  public int getStepsLeft() {
    return stepsLeft;
  }

  /** Roll two dice and begin a move phase. Disables further rolls until this turn completes. */
  public void rollDiceAndMove() {
    int roll = dice.roll();
    this.stepsLeft = roll;
    notifyObservers(
        currentPlayer.getName()
            + " rolled a "
            + roll
            + ". You have "
            + stepsLeft
            + " steps remaining.");
  }

  /**
   * Try to move the current player to the clicked tile. Only corridor→corridor or valid door→room
   * is allowed. Decrements stepsLeft and ends turn on room-entry or when stepsLeft hits zero.
   */
  public void movePlayerTo(GridPos target) {
    if (stepsLeft <= 0) {
      return;
    }

    GridPos here = currentPlayer.getPosition();

    var fromTile = boardModel.getTileAtPosition(here);
    var toTile = boardModel.getTileAtPosition(target);

    boolean adjacent =
        Math.abs(here.row() - target.row()) + Math.abs(here.col() - target.col()) == 1;

    boolean corridorToCorridor =
        fromTile instanceof CorridorTile && toTile instanceof CorridorTile && adjacent;

    boolean doorEntry =
        fromTile instanceof CorridorTile
            && toTile instanceof RoomTile
            && adjacent // must stand right outside the door
            && ((RoomTile) toTile).canEnterFrom(here.row(), here.col());

    boolean doorExit =
        fromTile instanceof RoomTile room
            && toTile instanceof CorridorTile
            && room.canExitTo(target.row(), target.col());

    // reject anything but corridor->corridor, corridor->room, room->corridor
    if (!(corridorToCorridor || doorEntry || doorExit)) {
      return;
    }

    // 1) move the model
    fromTile.removePlayer(currentPlayer);
    toTile.addPlayer(currentPlayer);
    currentPlayer.setPosition(target);

    // 2) decrement stepsLeft (but only zero out if *entering* a room)
    if (doorEntry) {
      stepsLeft = 0;
    } else {
      stepsLeft--;
    }

    // 3) notify
    notifyObservers(
        currentPlayer.getName() + " moved to " + target + ". " + stepsLeft + " steps left.");

    // 4) end the turn if out of steps
    if (stepsLeft == 0) {
      endTurn();
    }
  }

  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
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

  /** Called when this player’s movement finishes. Advances turn. */
  private void endTurn() {
    Player<GridPos> next = getNextPlayer();
    currentPlayer = next;
    notifyObservers("Turn over. It is now " + next.getName() + "'s turn.");
  }
}
