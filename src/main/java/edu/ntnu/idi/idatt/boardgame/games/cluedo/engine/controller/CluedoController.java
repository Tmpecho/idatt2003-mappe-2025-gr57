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
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.AccusationAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.MoveAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.RollAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Controller for the Cluedo game. Manages game flow, player turns, actions like moving, suggesting,
 * and accusing.
 */
public final class CluedoController extends GameController<GridPos> {
  private final CluedoBoard boardModel;
  private int stepsLeft = 0;
  private Suspect solutionSuspect;
  private Weapon solutionWeapon;
  private Room solutionRoom;
  private final Random rng = new SecureRandom();
  private final List<Suspect> suspects = List.of(Suspect.values());
  private final List<Player<GridPos>> turnOrder = new ArrayList<>();
  private int currentIndex;
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

    pickSolution();

    super.initialize(numberOfPlayers);

    currentIndex = 0;
    currentPlayer = turnOrder.get(currentIndex);

    dealRemainingCards();

    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int n) {
    LinkedHashMap<Integer, Player<GridPos>> map = new LinkedHashMap<>();
    for (int i = 1; i <= n; i++) {
      Suspect s = suspects.get((i - 1) % suspects.size());
      CluedoPlayer p = new CluedoPlayer(i, s.getName(), s.colour(), new GridPos(0, 0));
      map.put(i, p);
      turnOrder.add(p);
    }
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
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player<GridPos> getNextPlayer() {
    currentIndex = (currentIndex + 1) % turnOrder.size();
    return turnOrder.get(currentIndex);
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

  public void onAccuseButton(Suspect suspect, Weapon weapon, Room room) {
    new AccusationAction(this, suspect, weapon, room).execute();
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
      eliminateCurrentPlayer(currentPlayer);

      PauseTransition pause = new PauseTransition(Duration.seconds(2));
      pause.setOnFinished(
          e -> {
            phase = Phase.WAIT_ROLL;
            nextTurn();
          });
      pause.play();
    }
  }

  private void eliminateCurrentPlayer(Player<GridPos> p) {
    AbstractCluedoTile tile = boardModel.getTileAtPosition(p.getPosition());
    if (tile != null) {
      tile.removePlayer(p);
    }

    int removed = turnOrder.indexOf(p);
    turnOrder.remove(removed);
    if (removed <= currentIndex && currentIndex > 0) {
      currentIndex--;
    }

    notifyObservers(p.getName() + " has been eliminated and removed from the board.");
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
   * Selects the solution for the game by randomly picking one {@link Suspect}, one {@link Weapon},
   * and one {@link Room} from the available cards. The selected cards are then assigned to the
   * fields representing the solution of the game.
   */
  private void pickSolution() {
    var suspectList = Cards.shuffledSuspects(rng);
    solutionSuspect = suspectList.remove(0);

    var weaponList = Cards.shuffledWeapons(rng);
    solutionWeapon = weaponList.remove(0);

    var roomList = Cards.shuffledRooms(rng);
    solutionRoom = roomList.remove(0);
  }

  /**
   * Deal the remaining deck clockwise, one at a time, until empty.
   */
  private void dealRemainingCards() {
    List<Card> deck = new ArrayList<>();
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
