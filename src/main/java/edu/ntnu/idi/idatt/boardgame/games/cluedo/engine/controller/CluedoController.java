package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
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

/**
 * Controller for the Cluedo game. Manages game flow, player turns, actions like moving, suggesting,
 * and accusing.
 */
public final class CluedoController extends GameController<GridPos> {

  private final CluedoBoard boardModel;
  private final int numberOfPlayers;
  private int stepsLeft = 0;
  private List<Card> deck = new ArrayList<>();
  /**
   * The three cards (suspect, weapon, room) that form the solution to the mystery.
   */
  private final Card[] solution = new Card[3];
  private final Random rng = new SecureRandom();
  /**
   * List of available suspects, used for player creation.
   */
  private final List<Suspect> suspects = List.of(Suspect.values());

  private Phase phase = Phase.WAIT_ROLL;

  public boolean isWaitingForRoll() {
    return phase == Phase.WAIT_ROLL;
  }

  /**
   * Constructs a CluedoController.
   *
   * @param numberOfPlayers The number of players in the game (2-6).
   * @throws IllegalArgumentException if numberOfPlayers is not between 2 and 6.
   */
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
    // Game over when a correct accusation is made, or all but one player made wrong accusations.
    // TODO: Implement game over logic for Cluedo
    return false;
  }

  @Override
  protected void onGameFinish() {
    // TODO: Determine winner and notify
    notifyGameFinished(currentPlayer); // Placeholder
  }

  @Override
  protected Player<GridPos> getNextPlayer() {
    int currentId = currentPlayer.getId();
    int nextId = (currentId % numberOfPlayers) + 1;
    return players.get(nextId);
  }

  @Override
  public void saveGameState(String filePath) {
    // TODO: Implement Cluedo-specific game state saving
    System.out.println("Cluedo save game state not implemented yet for path: " + filePath);
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

  /**
   * True if the current player is in the “Cluedo” room and so may make an accusation.
   */
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
   * Try to move the current player to the clicked target {@link GridPos}. Movement is allowed
   * between adjacent corridor tiles, or between a corridor and an adjacent room if a valid door
   * exists. Entering a room typically ends the movement phase for the turn. Each step decrements
   * {@link #stepsLeft}.
   *
   * @param target The target {@link GridPos} to move to.
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

  /**
   * Gets the current player.
   *
   * @return The {@link Player} whose turn it is.
   */
  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Allows the current player to make a suggestion. This is typically done when the player is in a
   * room. The player suggests a suspect, a weapon, and the current room. Other players then attempt
   * to disprove the suggestion.
   */
  public void makeSuggestion() {
    // TODO: Implement suggestion logic (only in rooms, move suspect/weapon)
    notifyObservers(currentPlayer.getName() + " suggestion logic TBD.");
  }

  /**
   * Allows the current player to make an accusation. This can usually be done at any time on the
   * player's turn. If the accusation is correct, the player wins. If incorrect, the player may be
   * out of the game or unable to make further accusations, depending on house rules.
   */
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

  /**
   * Called when this player’s movement finishes. Advances turn.
   */
  private void endTurn() {
    Player<GridPos> next = getNextPlayer();
    currentPlayer = next;
    notifyObservers("Turn over. It is now " + next.getName() + "'s turn.");
  }

  /**
   * Build a complete shuffled deck and pick the three solution cards.
   */
  private void createCards() {
    deck = Cards.shuffledDeck(rng);

    solution[0] = drawCard(CardType.SUSPECT);
    solution[1] = drawCard(CardType.WEAPON);
    solution[2] = drawCard(CardType.ROOM);
  }

  // Helper to draw and remove the first card of a specific type from the main deck.
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

  /**
   * Deal the remaining deck clockwise, one at a time, until empty.
   */
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
