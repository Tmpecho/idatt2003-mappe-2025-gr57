package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.CardType;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Cards;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.MoveAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.RollAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public final class CluedoController extends GameController<GridPos> {
  private final CluedoBoard boardModel;
  private final int numberOfPlayers;
  private int stepsLeft = 0;
  private List<Card> deck = new ArrayList<>();
  private final Card[] solution = new Card[3];
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

    createCards();

    super.initialize(numberOfPlayers);

    distributeCards();

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
                  new CluedoPlayer(i, suspect.displayName(), suspect.colour(), new GridPos(0, 0));
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

  /** How many steps remain this turn. */
  public int getStepsLeft() {
    return stepsLeft;
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

    if (!boardModel.isLegalDestination(currentPlayer, target)) {
      return;
    }

    boolean enteringRoom = boardModel.getTileAtPosition(target) instanceof RoomTile;

    boardModel.setPlayerPosition(currentPlayer, target);
    stepsLeft--;

    String msg =
        currentPlayer.getName() + " moved to " + target + ". " + stepsLeft + " steps left.";
    notifyObservers(msg);

    if (enteringRoom) {
      stepsLeft = 0; // movement ends in a room
    }

    if (stepsLeft == 0) {
      phase = enteringRoom ? Phase.IN_ROOM : Phase.WAIT_ROLL;
      if (phase == Phase.WAIT_ROLL) {
        nextTurn(); // corridor -> turn ends
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

  public void makeAccusation() {
    // TODO: Implement accusation logic (check against solution, handle win/loss)
    notifyObservers(currentPlayer.getName() + " accusation logic TBD.");
    // if (isGameOver()) onGameFinish();
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

  /** Build a complete shuffled deck and pick the three solution cards. */
  private void createCards() {
    deck = Cards.shuffledDeck(rng);

    solution[0] = drawCard(CardType.SUSPECT);
    solution[1] = drawCard(CardType.WEAPON);
    solution[2] = drawCard(CardType.ROOM);
  }

  // Draws and removes the first card of the requested type from the deck.
  private Card drawCard(CardType wanted) {
    for (Iterator<Card> iterator = deck.iterator(); iterator.hasNext(); ) {
      Card card = iterator.next();
      if (card.type() == wanted) {
        iterator.remove();
        return card;
      }
    }
    throw new IllegalStateException("No card of type " + wanted + " left in deck.");
  }

  /** Deal the remaining deck clockwise, one at a time, until empty. */
  private void distributeCards() {
    List<CluedoPlayer> cluedoPlayers =
        players.values().stream()
            .map(player -> (CluedoPlayer) player)
            .sorted(Comparator.comparingInt(Player::getId))
            .toList();

    int idx = 0;
    while (!deck.isEmpty()) {
      cluedoPlayers.get(idx).addCard(deck.remove(0));
      idx = (idx + 1) % cluedoPlayers.size();
    }
  }
}
