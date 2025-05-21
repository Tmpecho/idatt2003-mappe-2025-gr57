package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Cards;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.MoveAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.RollAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public final class CluedoController extends GameController<GridPos> {
  private final CluedoBoard boardModel;
  private final int numberOfPlayers;
  private int stepsLeft = 0;
  private List<Card> deck = new ArrayList<>();
  private Suspect solutionSuspect;
  private Weapon solutionWeapon;
  private Room solutionRoom;
  private final Random rng = new SecureRandom();
  private final List<Suspect> suspects = List.of(Suspect.values());

  private Phase phase = Phase.WAIT_ROLL;

  public boolean isWaitingForRoll() {
    return phase == Phase.WAIT_ROLL;
  }

  public CluedoController(int numberOfPlayers) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;

    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }

    this.numberOfPlayers = numberOfPlayers;

    pickSolution();

    super.initialize(numberOfPlayers);

    dealRemainingCards();

    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int n) {
    Map<Integer, Player<GridPos>> map = new HashMap<>();
    IntStream.rangeClosed(1, n)
        .forEach(
            i -> {
              Suspect suspect = suspects.get(i - 1);
              CluedoPlayer player =
                  new CluedoPlayer(i, suspect.getName(), suspect.colour(), new GridPos(0, 0));
              map.put(i, player);
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

  public void beginMovePhase(int rolled) {
    this.stepsLeft = rolled;
    this.phase = Phase.MOVING;

    notifyObservers(
        currentPlayer.getName() + " rolled " + rolled + ". Click a neighbouring square to move.");
  }

  /**
   * True if the current player is in a normal room (not the central “Cluedo” room) and so may make
   * a suggestion.
   */
  public boolean canSuggest() {
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    if (tile instanceof RoomTile room) {
      return !"Cluedo".equals(room.getRoomName());
    }
    return false;
  }

  /** True if the current player is in the “Cluedo” room and so may make an accusation. */
  public boolean canAccuse() {
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    return tile instanceof RoomTile room && "Cluedo".equals(room.getRoomName());
  }

  public void onRollButton() {
    new RollAction(this, dice).execute();
  }

  public void onBoardClick(GridPos target) {
    new MoveAction(this, target).execute();
  }

  /**
   * Try to move the current player to the clicked tile. Only corridor→corridor or valid door→room
   * is allowed. Decrements stepsLeft and ends turn on room-entry or when stepsLeft hits zero.
   */
  public void movePlayerTo(GridPos target) {
    if (phase != Phase.MOVING || stepsLeft <= 0) {
      return;
    }

    GridPos currentPosition = currentPlayer.getPosition();
    if (!boardModel.isLegalDestination(currentPosition, target)) {
      return;
    }

    // did we step *into* a room?
    boolean enteringRoom = boardModel.getTileAtPosition(target) instanceof RoomTile;

    // 1) actually move
    boardModel.setPlayerPosition(currentPlayer, target);

    // 2) consume a step (or zero out on entry)
    if (enteringRoom) {
      stepsLeft = 0;
    } else {
      stepsLeft--;
    }

    // 3) log it
    notifyObservers(
        currentPlayer.getName() + " moved to " + target + ". " + stepsLeft + " steps left.");

    // 4) adjust phase / turn
    if (stepsLeft == 0) {
      if (enteringRoom) {
        phase = Phase.IN_ROOM;
      } else {
        phase = Phase.WAIT_ROLL;
        nextTurn();
      }
    }
  }

  private void nextTurn() {
    currentPlayer = getNextPlayer();
    notifyObservers("Turn over. " + currentPlayer.getName() + " to roll.");
  }

  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
  }

  public void makeSuggestion() {
    // TODO: Implement suggestion logic (only in rooms, move suspect/weapon)
    notifyObservers(currentPlayer.getName() + " suggestion logic TBD.");
  }

  public void makeAccusation(Suspect suspect, Weapon weapon, Room room) {
    if (suspect == null || weapon == null || room == null) {
      throw new IllegalArgumentException("Accusation cannot be null.");
    }

    if (suspect == solutionSuspect && weapon == solutionWeapon && room == solutionRoom) {
      notifyObservers(currentPlayer.getName() + " wins!");
      onGameFinish();
    } else {
      notifyObservers(
          currentPlayer.getName()
              + " accused "
              + suspect.getName()
              + " with "
              + weapon.getName()
              + " in "
              + room.getName()
              + ". Wrong!");
    }
  }

  private enum Phase {
    WAIT_ROLL,
    MOVING,
    IN_ROOM,
    TURN_OVER
  }

  /** Called when this player’s movement finishes. Advances turn. */
  private void endTurn() {
    Player<GridPos> next = getNextPlayer();
    currentPlayer = next;
    notifyObservers("Turn over. It is now " + next.getName() + "'s turn.");
  }

  private void pickSolution() {
    // grab one of each, remove it from the pool
    var sList = Cards.shuffledSuspects(rng);
    solutionSuspect = sList.remove(0);

    var wList = Cards.shuffledWeapons(rng);
    solutionWeapon = wList.remove(0);

    var rList = Cards.shuffledRooms(rng);
    solutionRoom = rList.remove(0);

    // now sList, wList, rList hold the cards you will deal
  }

  private void dealRemainingCards() {
    List<Card> deck = new ArrayList<>(); // todo: use a proper type
    deck.addAll(Cards.shuffledSuspects(rng));
    deck.addAll(Cards.shuffledWeapons(rng));
    deck.addAll(Cards.shuffledRooms(rng));
    Collections.shuffle(deck, rng);

    var playersInOrder = players.values().stream().map(p -> (CluedoPlayer) p).toList();

    int idx = 0;
    for (Card card : deck) {
      CluedoPlayer player = playersInOrder.get(idx);
      if (card instanceof Suspect suspect) {
        player.addCard(suspect);
      } else if (card instanceof Weapon weapon) {
        player.addCard(weapon);
      } else if (card instanceof Room room) {
        player.addCard(room);
      }
      idx = (idx + 1) % playersInOrder.size();
    }
  }
}
