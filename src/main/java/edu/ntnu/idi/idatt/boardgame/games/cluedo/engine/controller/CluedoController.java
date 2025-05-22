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
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.SuggestionAction;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CluedoController extends GameController<GridPos> {

  private final CluedoBoard boardModel;
  private int stepsLeft = 0;
  private Suspect solutionSuspect;
  private Weapon solutionWeapon;
  private Room solutionRoom;
  private final Random rng = new SecureRandom();
  // private final List<Suspect> suspects = List.of(Suspect.values()); // No longer needed for createPlayers
  private final List<Player<GridPos>> turnOrder = new ArrayList<>();
  private int currentIndex = 0; // Current index in turnOrder
  private Phase phase = Phase.WAIT_ROLL;

  private static final Logger logger = LoggerFactory.getLogger(CluedoController.class);

  /**
   * Constructs a CluedoController with custom player details.
   *
   * @param playerDetailsList List of player configurations.
   * @throws IllegalArgumentException if playerDetailsList size is not between 2 and 6.
   */
  public CluedoController(List<PlayerSetupDetails> playerDetailsList) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;

    if (playerDetailsList.size() < 2 || playerDetailsList.size() > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }

    initializeGame(playerDetailsList);

    pickSolution();
    dealRemainingCards();

    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  /**
   * @deprecated Use the constructor with PlayerSetupDetails.
   */
  @Deprecated
  public CluedoController(int numberOfPlayers) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;

    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }
    initialize(numberOfPlayers); // Calls old deprecated GameController.initialize

    pickSolution();
    dealRemainingCards();
    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Override
  protected Map<Integer, Player<GridPos>> setupPlayers(List<PlayerSetupDetails> playerDetailsList) {
    LinkedHashMap<Integer, Player<GridPos>> newPlayersMap = new LinkedHashMap<>();
    this.turnOrder.clear(); // Clear any previous turn order
    AtomicInteger playerIdCounter = new AtomicInteger(1);

    for (PlayerSetupDetails detail : playerDetailsList) {
      int id = playerIdCounter.getAndIncrement();
      String name = detail.name();
      Suspect suspect = detail.suspectIfCluedo().orElseThrow(
          () -> new IllegalArgumentException("Suspect details missing for Cluedo player: " + name)
      );
      CluedoPlayer player = new CluedoPlayer(id, name, suspect.colour(),
          new GridPos(0, 0) /*dummy start*/);
      newPlayersMap.put(id, player);
      this.turnOrder.add(player);
    }

    if (!this.turnOrder.isEmpty()) {
      this.currentIndex = 0;
      this.currentPlayer = this.turnOrder.get(this.currentIndex);
    } else {
      throw new IllegalStateException("No players were created for Cluedo.");
    }
    return newPlayersMap;
  }


  /**
   * @deprecated Use setupPlayers(List<PlayerSetupDetails>) with the new initialization flow.
   */
  @Deprecated
  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int n) {
    final List<Suspect> defaultSuspectOrder = List.of(Suspect.values());
    LinkedHashMap<Integer, Player<GridPos>> map = new LinkedHashMap<>();
    this.turnOrder.clear();

    for (int i = 1; i <= n; i++) {
      Suspect s = defaultSuspectOrder.get((i - 1) % defaultSuspectOrder.size());
      CluedoPlayer p = new CluedoPlayer(i, s.getName(), s.colour(), new GridPos(0, 0));
      map.put(i, p);
      this.turnOrder.add(p);
    }
    if (!turnOrder.isEmpty()) {
      this.currentIndex = 0;
      // currentPlayer will be set by GameController.initialize for this old path
    }
    return map;
  }


  @Override
  public boolean isGameOver() {
    return turnOrder.size() <= 1;
  }

  @Override
  protected void onGameFinish() {
    phase = Phase.TURN_OVER; // Mark as game over
    notifyGameFinished(currentPlayer);
  }

  @Override
  protected Player<GridPos> getNextPlayer() {
    if (turnOrder.isEmpty()) {
      logger.error("Attempting to get next player from an empty turn order.");
      throw new IllegalStateException("No players left in turn order.");
    }
    currentIndex = (currentIndex + 1) % turnOrder.size();
    return turnOrder.get(currentIndex);
  }

  @Override
  public void saveGameState(String filePath) {
    logger.warn("Cluedo save game state not implemented yet for path: {}", filePath);
  }

  @Override
  public void loadGameState(String filePath) {
    logger.warn("Cluedo load game state not implemented yet from path: {}", filePath);
  }

  public boolean isWaitingForRoll() {
    return phase == Phase.WAIT_ROLL;
  }

  public void beginMovePhase(int rolled) {
    this.stepsLeft = rolled;
    this.phase = Phase.MOVING;
    notifyObservers(
        currentPlayer.getName() + " rolled " + rolled + ". Click a neighbouring square to move.");
  }

  public boolean canSuggest() {
    if (phase != Phase.IN_ROOM && phase != Phase.MOVING && stepsLeft == 0) {
      return false; // Can suggest if move ends in room
    }
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    if (tile instanceof RoomTile room) {
      return !"Cluedo".equals(room.getRoomName());
    }
    return false;
  }

  public boolean canAccuse() {
    if (phase != Phase.IN_ROOM && phase != Phase.MOVING && stepsLeft == 0) {
      return false;
    }
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    return tile instanceof RoomTile room && "Cluedo".equals(room.getRoomName());
  }

  public void onRollButton() {
    if (phase != Phase.WAIT_ROLL) {
      logger.warn("Roll button clicked in invalid phase: {}", phase);
      return;
    }
    new RollAction(this, dice).execute();
  }

  public void onBoardClick(GridPos target) {
    if (phase != Phase.MOVING) {
      logger.warn("Board clicked in invalid phase: {}", phase);
      return;
    }
    new MoveAction(this, target).execute();
  }

  public void onAccuseButton(Suspect suspect, Weapon weapon, Room room) {
    if (!canAccuse()) {
      logger.warn("Accuse button clicked when accusation is not allowed (Phase: {}).", phase);
      return;
    }
    new AccusationAction(this, suspect, weapon, room).execute();
  }

  public void onSuggestButton(Suspect suspect, Weapon weapon, Room room) {
    if (!canSuggest()) {
      logger.warn("Suggest button clicked when suggestion is not allowed (Phase: {}).", phase);
      return;
    }
    new SuggestionAction(this, suspect, weapon, room).execute();
  }

  public void movePlayerTo(GridPos target) {
    if (phase != Phase.MOVING || stepsLeft <= 0) {
      return;
    }
    if (!boardModel.isLegalDestination(currentPlayer.getPosition(), target)) {
      return;
    }

    boolean enteringRoom = boardModel.getTileAtPosition(target) instanceof RoomTile;
    boardModel.setPlayerPosition(currentPlayer, target);
    if (enteringRoom) {
      stepsLeft = 0;
    } else {
      stepsLeft--;
    }

    notifyObservers(
        currentPlayer.getName() + " moved to " + target + ". " + stepsLeft + " steps left.");

    if (stepsLeft == 0) {
      if (enteringRoom) {
        phase = Phase.IN_ROOM;
        notifyObservers(
            currentPlayer.getName() + " is in a room. Make a suggestion/accusation or end turn.");
      } else {
        endTurn();
      }
    }
  }

  private void nextTurn() {
    if (turnOrder.isEmpty() || isGameOver()) {
      logger.info("Game is over or no players left, not starting next turn.");
      if (!isGameOver()) {
        onGameFinish();
      }
      return;
    }
    currentPlayer = getNextPlayer();
    phase = Phase.WAIT_ROLL;
    notifyObservers("Turn over. " + currentPlayer.getName() + " to roll.");
  }

  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
  }

  public void makeSuggestion(Suspect suggestedSuspect, Weapon suggestedWeapon, Room suggestedRoom) {
    if (suggestedSuspect == null || suggestedWeapon == null || suggestedRoom == null) {
      throw new IllegalArgumentException("Suggestion cannot be null.");
    }
    if (phase != Phase.IN_ROOM) {
      logger.warn("Suggestion made in invalid phase: {}", phase);
      return;
    }

    int totalPlayersInOrder = turnOrder.size();
    boolean disproved = false;
    for (int i = 1; i < totalPlayersInOrder; i++) {
      int respondentIdx = (this.currentIndex + i) % totalPlayersInOrder;
      CluedoPlayer respondent = (CluedoPlayer) turnOrder.get(respondentIdx);

      if (respondent == currentPlayer) {
        continue; // Skip self
      }

      List<Card> heldMatchingCards = new ArrayList<>();
      if (respondent.hasCard(suggestedSuspect)) {
        heldMatchingCards.add(suggestedSuspect);
      }
      if (respondent.hasCard(suggestedWeapon)) {
        heldMatchingCards.add(suggestedWeapon);
      }
      if (respondent.hasCard(suggestedRoom)) {
        heldMatchingCards.add(suggestedRoom);
      }

      if (!heldMatchingCards.isEmpty()) {
        Card shownCard = heldMatchingCards.get(rng.nextInt(heldMatchingCards.size()));
        notifyObservers(
            currentPlayer.getName()
                + " suggested " + suggestedSuspect.getName() + " in the "
                + suggestedRoom.getName() + " with the " + suggestedWeapon.getName() + ". "
                + respondent.getName() + " disproved by showing \"" + shownCard.getName() + ".\"");
        disproved = true;
        break;
      }
    }

    if (!disproved) {
      notifyObservers(
          currentPlayer.getName()
              + " suggested " + suggestedSuspect.getName() + " in the "
              + suggestedRoom.getName() + " with the " + suggestedWeapon.getName()
              + ". No one could disprove the suggestion.");
    }
  }

  public void makeAccusation(Suspect suspect, Weapon weapon, Room room) {
    if (suspect == null || weapon == null || room == null) {
      throw new IllegalArgumentException("Accusation cannot be null.");
    }
    if (phase != Phase.IN_ROOM || !("Cluedo".equals(getRoomOfCurrentPlayerName()))) {
      logger.warn("Accusation made in invalid phase ({}) or location ({}).", phase,
          getRoomOfCurrentPlayerName());
      return;
    }

    if (suspect == solutionSuspect && weapon == solutionWeapon && room == solutionRoom) {
      notifyObservers(currentPlayer.getName() + " wins! The solution was indeed " +
          solutionSuspect.getName() + " with the " + solutionWeapon.getName() + " in the " +
          solutionRoom.getName() + ".");
      onGameFinish();
    } else {
      notifyObservers(
          currentPlayer.getName()
              + " accused " + suspect.getName() + " with " + weapon.getName()
              + " in " + room.getName() + ". This is WRONG!");
      eliminateCurrentPlayer(currentPlayer);

      if (isGameOver()) {
        onGameFinish();
      } else {
        PauseTransition pause = new PauseTransition(Duration.seconds(1)); // Shorter delay
        pause.setOnFinished(event -> nextTurn());
        pause.play();
      }
    }
  }

  private void eliminateCurrentPlayer(Player<GridPos> p) {
    AbstractCluedoTile tile = boardModel.getTileAtPosition(p.getPosition());
    if (tile != null) {
      tile.removePlayer(p);
    }

    int removedPlayerIndexInTurnOrder = turnOrder.indexOf(p);
    if (removedPlayerIndexInTurnOrder != -1) {
      turnOrder.remove(removedPlayerIndexInTurnOrder);
      if (removedPlayerIndexInTurnOrder < this.currentIndex) {
        this.currentIndex--;
      } else if (removedPlayerIndexInTurnOrder == this.currentIndex) {
      }
      notifyObservers(p.getName() + " has been eliminated and removed from the board.");
    } else {
      logger.warn("Attempted to eliminate player {} not found in turn order.", p.getName());
    }

    if (turnOrder.size() == 1 && !isGameOver()) {
      this.currentPlayer = turnOrder.get(0);
      notifyObservers(
          this.currentPlayer.getName() + " is the last one remaining and wins by default!");
      onGameFinish();
    } else if (turnOrder.isEmpty() && !isGameOver()) {
      notifyObservers("All players have been eliminated. The house wins!");
      onGameFinish();
    }
  }


  public Room getRoomOfCurrentPlayer() {
    String roomName = getRoomOfCurrentPlayerName();
    if (roomName != null) {
      try {
        return Room.fromDisplayName(roomName);
      } catch (IllegalArgumentException e) {
        logger.error("Player is in a room tile with an unmappable name: {}", roomName, e);
        return null;
      }
    }
    return null;
  }

  private String getRoomOfCurrentPlayerName() {
    if (currentPlayer == null) {
      return null;
    }
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    if (tile instanceof RoomTile roomTile) {
      return roomTile.getRoomName();
    }
    return null;
  }


  public void endTurn() {
    if (phase == Phase.TURN_OVER) {
      return;
    }
    this.stepsLeft = 0;
    nextTurn();
  }

  private void pickSolution() {
    var localSuspectList = Cards.shuffledSuspects(rng);
    solutionSuspect = localSuspectList.remove(0);

    var localWeaponList = Cards.shuffledWeapons(rng);
    solutionWeapon = localWeaponList.remove(0);

    var localRoomList = Cards.shuffledRooms(rng);
    solutionRoom = localRoomList.remove(0);

    logger.info("Solution picked: {} with {} in {}", solutionSuspect.getName(),
        solutionWeapon.getName(), solutionRoom.getName());
  }


  private void dealRemainingCards() {
    List<Card> deck = new ArrayList<>();
    for (Suspect s : Suspect.values()) {
      if (s != solutionSuspect) {
        deck.add(s);
      }
    }
    for (Weapon w : Weapon.values()) {
      if (w != solutionWeapon) {
        deck.add(w);
      }
    }
    for (Room r : Room.values()) {
      if (r != solutionRoom) {
        deck.add(r);
      }
    }

    Collections.shuffle(deck, rng);

    if (turnOrder.isEmpty()) {
      logger.error("Cannot deal cards, no players in turn order.");
      return;
    }

    int playerIdx = 0;
    for (Card card : deck) {
      CluedoPlayer cluedoPlayer = (CluedoPlayer) turnOrder.get(playerIdx);
      if (card instanceof Suspect suspectCard) {
        cluedoPlayer.addCard(suspectCard);
      } else if (card instanceof Weapon weaponCard) {
        cluedoPlayer.addCard(weaponCard);
      } else if (card instanceof Room roomCard) {
        cluedoPlayer.addCard(roomCard);
      }
      playerIdx = (playerIdx + 1) % turnOrder.size();
    }
  }

  private enum Phase {
    WAIT_ROLL,
    MOVING,
    IN_ROOM,
    TURN_OVER
  }
}
