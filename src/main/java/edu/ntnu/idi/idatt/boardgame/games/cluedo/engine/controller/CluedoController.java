package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.core.persistence.GameStateRepository;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Cards;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.Phase;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.AccusationAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.MoveAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.RollAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action.SuggestionAction;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.dto.CluedoGameStateDto;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.persistence.mapper.CluedoMapper;
import edu.ntnu.idi.idatt.boardgame.ui.dto.PlayerSetupDetails;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Cluedo game. Manages game flow, player turns, actions like moving, suggesting,
 * and accusing.
 */
public final class CluedoController extends GameController<GridPos> {

  /**
   * Repository for saving and loading game state.
   */
  private final GameStateRepository<CluedoGameStateDto> repo;
  private final CluedoBoard boardModel;
  private int stepsLeft = 0;
  private Suspect solutionSuspect;
  private Weapon solutionWeapon;
  private Room solutionRoom;
  private final Random rng = new SecureRandom();
  private final List<Player<GridPos>> turnOrder = new ArrayList<>();
  private int currentIndex = 0;
  private Phase phase = Phase.WAIT_ROLL;

  private static final Logger logger = LoggerFactory.getLogger(CluedoController.class);

  /**
   * Constructs a CluedoController with the specified player details and game state repository.
   *
   * @param playerDetailsList List of player setup details. Can be null/empty for loading.
   * @param repo              Repository for saving and loading game state.
   */
  public CluedoController(List<PlayerSetupDetails> playerDetailsList,
      GameStateRepository<CluedoGameStateDto> repo) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;
    this.repo = Objects.requireNonNull(repo);

    boolean isNewGameSetup = playerDetailsList != null && !playerDetailsList.isEmpty();

    if (isNewGameSetup) {
      if (playerDetailsList.size() < 2 || playerDetailsList.size() > 6) {
        throw new IllegalArgumentException("Cluedo requires 2 to 6 players for a new game.");
      }
    }

    initializeGame(playerDetailsList); // Calls our overridden setupPlayers

    if (isNewGameSetup) {
      pickSolution();
      dealRemainingCards();
      notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
    }
    // For loading, observers are notified by loadGameState after state is fully restored.
  }


  @Override
  protected Map<Integer, Player<GridPos>> setupPlayers(List<PlayerSetupDetails> playerDetailsList) {
    LinkedHashMap<Integer, Player<GridPos>> newPlayersMap = new LinkedHashMap<>();
    this.turnOrder.clear(); // Clear before setup or load

    if (playerDetailsList == null || playerDetailsList.isEmpty()) {
      // This indicates a loading scenario.
      // Players will be populated by loadGameState.
      // CurrentPlayer will also be set by loadGameState.
      return newPlayersMap; // Return empty map
    }

    AtomicInteger playerIdCounter = new AtomicInteger(1);
    for (PlayerSetupDetails detail : playerDetailsList) {
      int id = playerIdCounter.getAndIncrement();
      String name = detail.name();
      Suspect suspect = detail.suspectIfCluedo().orElseThrow(
          () -> new IllegalArgumentException("Suspect details missing for Cluedo player: " + name)
      );
      // Start position is nominal; actual placement is handled by gameBoard.addPlayersToStart
      CluedoPlayer player = new CluedoPlayer(id, name, suspect.colour(), new GridPos(0, 0));
      newPlayersMap.put(id, player);
      this.turnOrder.add(player);
    }

    if (!this.turnOrder.isEmpty()) {
      this.currentIndex = 0; // Reset for new game
      this.currentPlayer = this.turnOrder.get(this.currentIndex);
    } else { // This case should ideally not be hit if playerDetailsList was valid for a new game
      throw new IllegalStateException(
          "No players were created for Cluedo from non-empty playerDetailsList.");
    }
    return newPlayersMap;
  }

  @Override
  public boolean isGameOver() {
    return phase == Phase.TURN_OVER || turnOrder.size() <= 1;
  }

  @Override
  protected void onGameFinish() {
    if (phase != Phase.TURN_OVER) {
      phase = Phase.TURN_OVER;
      notifyGameFinished(currentPlayer);
    }
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
  public void saveGameState(String path) {
    try {
      repo.save(CluedoMapper.toDto(this), Path.of(path));
      LoggingNotification.info("Game Saved", "Game state saved to " + path);
    } catch (Exception e) {
      logger.error("Save failed: {}", e.getMessage(), e);
      LoggingNotification.error("Save failed", e.getMessage());
    }
  }

  @Override
  public void loadGameState(String path) {
    try {
      CluedoGameStateDto dto = repo.load(Path.of(path));

      CluedoBoard board = (CluedoBoard) this.gameBoard;
      for (int r = 0; r < board.getBoardSize(); r++) {
        for (int c = 0; c < board.getBoardSize(); c++) {
          AbstractCluedoTile tile = board.getTileAtPosition(new GridPos(r, c));
          if (tile != null && !tile.getPlayers().isEmpty()) {
            new ArrayList<>(tile.getPlayers()).forEach(tile::removePlayer);
          }
        }
      }

      Map<Integer, Player<GridPos>> loadedPlayers = new LinkedHashMap<>();
      this.turnOrder.clear();

      for (CluedoGameStateDto.PlayerState ps : dto.players) {
        PlayerColor playerColor = PlayerColor.valueOf(ps.colour);
        Suspect suspect = Suspect.from(playerColor);
        // Actual name from Suspect might be better than "Player X" if details were stored
        CluedoPlayer player = new CluedoPlayer(ps.id, suspect.getName(), playerColor,
            new GridPos(ps.row, ps.col));
        loadedPlayers.put(ps.id, player);
        this.turnOrder.add(player); // Rebuild turnOrder based on DTO's player list order
      }
      this.players = loadedPlayers;

      CluedoMapper.apply(dto, this); // This will set solution, cards, notes, currentPlayer etc.

      // Ensure currentPlayer and currentIndex are correctly set after loading
      if (this.currentPlayer != null && this.turnOrder.contains(this.currentPlayer)) {
        this.currentIndex = this.turnOrder.indexOf(this.currentPlayer);
      } else if (!this.turnOrder.isEmpty()) {
        logger.warn(
            "Loaded currentPlayer not in turnOrder or turnOrder empty after apply."
                + " Resetting currentIndex.");
        this.currentIndex = 0; // Or find current player ID from DTO and set index
        this.currentPlayer = this.turnOrder.get(this.currentIndex); // Fallback
      } else {
        logger.error("Failed to set current player after load - turn order is empty.");
      }

      String message = "Game state loaded.";
      if (currentPlayer != null) {
        if (phase == Phase.WAIT_ROLL) {
          message += " " + currentPlayer.getName() + " to roll.";
        } else if (phase == Phase.MOVING) {
          message += " " + currentPlayer.getName() + " to move with " + stepsLeft + " steps left.";
        } else {
          message += " Current turn: " + currentPlayer.getName() + ". Phase: " + phase;
        }
      } else {
        message += " No current player found after load.";
      }
      notifyObservers(message);
    } catch (Exception e) {
      logger.error("Load failed: {}", e.getMessage(), e);
      LoggingNotification.error("Load failed", e.getMessage());
    }
  }


  /**
   * Determines whether the game is in the "WAIT_ROLL" phase.
   *
   * @return true if the current game phase is "WAIT_ROLL", false otherwise.
   */
  public boolean isWaitingForRoll() {
    return phase == Phase.WAIT_ROLL;
  }

  public boolean isNotWaitingForRoll() {
    return phase != Phase.WAIT_ROLL;
  }

  /**
   * Initiates the movement phase for the current player after they have rolled the dice. Sets the
   * number of steps the player can take and updates the game phase to MOVING. Notifies observers
   * about the roll and available steps.
   *
   * @param rolled The number of steps the player rolled on the dice.
   */
  public void beginMovePhase(int rolled) {
    this.stepsLeft = rolled;
    this.phase = Phase.MOVING;

    notifyObservers(
        currentPlayer.getName() + " rolled " + rolled + ". Click a neighbouring square to move.");
  }

  /**
   * True if the current player is in a normal room (not the central “Cluedo” room) and so may make
   * a suggestion.
   *
   * @return true if the player can make a suggestion, false otherwise.
   */
  public boolean canSuggest() {
    if (phase != Phase.IN_ROOM && !(phase == Phase.MOVING && stepsLeft == 0)) {
      return false;
    }
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    if (tile instanceof RoomTile room) {
      return !"Cluedo".equals(room.getRoomName());
    }
    return false;
  }

  /**
   * True if the current player is in the central “Cluedo” room and so may make an accusation.
   *
   * @return true if the player can make an accusation, false otherwise.
   */
  public boolean canAccuse() {
    if (phase != Phase.IN_ROOM && !(phase == Phase.MOVING && stepsLeft == 0)) {
      return false;
    }
    GridPos pos = currentPlayer.getPosition();
    AbstractCluedoTile tile = boardModel.getTileAtPosition(pos);
    return tile instanceof RoomTile room && "Cluedo".equals(room.getRoomName());
  }

  /**
   * True if the current player is not in the central “Cluedo” room and so may not make an
   * accusation.
   *
   * @return true if the player cannot make an accusation, false otherwise.
   */
  public boolean canNotAccuse() {
    return !canAccuse();
  }

  /**
   * Handles the action triggered when the roll dice button is pressed. Executes a
   * {@link RollAction} for the current player.
   */
  public void onRollButton() {
    if (phase != Phase.WAIT_ROLL) {
      logger.warn("Roll button clicked in invalid phase: {}", phase);
      return;
    }
    new RollAction(this, dice).execute();
  }

  /**
   * Handles the action triggered when a tile on the game board is clicked. Executes a
   * {@link MoveAction} for the current player towards the target position.
   *
   * @param target The {@link GridPos} of the clicked tile.
   */
  public void onBoardClick(GridPos target) {
    if (phase != Phase.MOVING) {
      logger.warn("Board clicked in invalid phase: {}", phase);
      return;
    }
    new MoveAction(this, target).execute();
  }

  /**
   * Handles the action triggered when the accuse button is pressed. Executes an
   * {@link AccusationAction} with the provided suspect, weapon, and room.
   *
   * @param suspect The suspected character.
   * @param weapon  The suspected weapon.
   * @param room    The room where the crime is suspected to have occurred.
   */
  public void onAccuseButton(Suspect suspect, Weapon weapon, Room room) {
    if (!canAccuse()) {
      logger.warn("Accuse button clicked when accusation is not allowed (Phase: {}).", phase);
      LoggingNotification.warn("Cannot Accuse", "You must be in the 'Cluedo' room to accuse.");
      return;
    }
    new AccusationAction(this, suspect, weapon, room).execute();
  }

  /**
   * Handles the action triggered when the suggest button is pressed. Executes a
   * {@link SuggestionAction} with the provided suspect, weapon, and room.
   *
   * @param suspect The suspected character involved in the suggestion.
   * @param weapon  The suspected weapon used in the suggestion.
   * @param room    The room where the suggestion is being made.
   */
  public void onSuggestButton(Suspect suspect, Weapon weapon, Room room) {
    if (!canSuggest()) {
      logger.warn("Suggest button clicked when suggestion is not allowed (Phase: {}).", phase);
      LoggingNotification.warn("Cannot Suggest",
          "You must be in a regular room to make a suggestion.");
      return;
    }
    new SuggestionAction(this, suspect, weapon, room).execute();
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
        String roomName = ((RoomTile) boardModel.getTileAtPosition(target)).getRoomName();
        notifyObservers(
            currentPlayer.getName() + " entered the " + roomName
                + ". Make a suggestion/accusation or end turn.");
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
    stepsLeft = 0;
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
  public void makeSuggestion(Suspect suggestedSuspect, Weapon suggestedWeapon, Room suggestedRoom) {
    if (suggestedSuspect == null || suggestedWeapon == null || suggestedRoom == null) {
      throw new IllegalArgumentException("Suggestion cannot be null.");
    }

    // Move the suggested player (if they are not the current player) to the current room
    Player<GridPos> playerToMove = null;
    for (Player<GridPos> p : players.values()) {
      if (p instanceof CluedoPlayer cp) {
        // Assuming Player names are set to Suspect display names during setup
        if (cp.getName().equals(suggestedSuspect.getName())) {
          playerToMove = p;
          break;
        }
      }
    }

    if (playerToMove != null && playerToMove != currentPlayer) {
      boardModel.setPlayerPosition(playerToMove, currentPlayer.getPosition());
      LoggingNotification.info("Player Moved",
          suggestedSuspect.getName() + " has been moved to the " + suggestedRoom.getName()
              + " for the suggestion.");
    }

    int totalPlayersInOrder = turnOrder.size();
    boolean disproved = false;
    for (int i = 1; i < totalPlayersInOrder; i++) {
      int respondentIdx = (this.currentIndex + i) % totalPlayersInOrder;
      CluedoPlayer respondent = (CluedoPlayer) turnOrder.get(respondentIdx);

      if (respondent == currentPlayer) { // Should not happen if iteration is correct, but safeguard
        continue;
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

  /**
   * Allows the current player to make an accusation.
   *
   * @param suspect The suspected character.
   * @param weapon  The suspected weapon.
   * @param room    The room where the crime is suspected to have occurred.
   */
  public void makeAccusation(Suspect suspect, Weapon weapon, Room room) {
    if (suspect == null || weapon == null || room == null) {
      throw new IllegalArgumentException("Accusation cannot be null.");
    }

    if (suspect == solutionSuspect && weapon == solutionWeapon && room == solutionRoom) {
      notifyObservers(currentPlayer.getName() + " wins! The solution was indeed "
          + solutionSuspect.getName() + " with the " + solutionWeapon.getName() + " in the "
          + solutionRoom.getName() + ".");
      onGameFinish();
    } else {
      notifyObservers(
          currentPlayer.getName()
              + " accused " + suspect.getName() + " with " + weapon.getName()
              + " in " + room.getName() + ". This is WRONG!");
      eliminateCurrentPlayer(currentPlayer);

      if (isGameOver()) {
        if (phase != Phase.TURN_OVER) {
          onGameFinish();
        }
      } else {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
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
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
          currentIndex = turnOrder.size() - 1; // or 0 if it should wrap to start
        }
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
      onGameFinish(); // Ensure game ends
    }
  }

  /**
   * Retrieves the room in which the current player is located. This method checks the tile at the
   * player's current position on the board. If the tile corresponds to a room, it converts the
   * room's display name into a {@link Room} object and returns it. If the player is not in a room,
   * the method returns null.
   *
   * @return The {@link Room} object representing the current player's location if they are in a
   * room, or null if the player is not in a room.
   */
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

  /**
   * Called when this player’s movement finishes. Advances turn.
   */
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

  public Map<Integer, Player<GridPos>> getPlayers() {
    return this.players.values().stream()
        .collect(
            Collectors.toMap(Player::getId, player -> player, (a, b) -> b, LinkedHashMap::new));
  }

  /**
   * Sets the current player in the game and updates their position in the turn order.
   *
   * @param currentPlayer The {@link CluedoPlayer} to set as the current player.
   */
  public void setCurrentPlayer(CluedoPlayer currentPlayer) {
    this.currentPlayer = currentPlayer;
    if (currentPlayer != null && this.turnOrder.contains(currentPlayer)) {
      this.currentIndex = this.turnOrder.indexOf(currentPlayer);
    } else if (currentPlayer != null) {
      logger.warn(
          "setCurrentPlayer called with a player not found in turnOrder or turnOrder is "
              + "inconsistent. Player: {}, TurnOrder size: {}",
          currentPlayer.getName(), this.turnOrder.size());
      // Attempt to find by ID if names differ, or re-evaluate turnOrder setup during load
      boolean found = false;
      for (int i = 0; i < this.turnOrder.size(); i++) {
        if (this.turnOrder.get(i).getId() == currentPlayer.getId()) {
          this.currentIndex = i;
          found = true;
          break;
        }
      }
      if (!found && !this.turnOrder.isEmpty()) {
        logger.warn("Fallback: Setting currentIndex to 0 as player ID {} not in turnOrder.",
            currentPlayer.getId());
        this.currentIndex = 0; // Fallback, may need adjustment if DTO stores turn order explicitly
      } else if (this.turnOrder.isEmpty()) {
        logger.error("Cannot set current player; turnOrder is empty.");
      }
    }
  }

  /**
   * Retrieves the current phase of the game.
   *
   * @return The current phase of the game as a {@link Phase} enum value.
   */
  public Phase getPhase() {
    return phase;
  }

  /**
   * Updates the current phase of the game.
   *
   * @param phase The new phase to set, as a {@link Phase} enum value.
   */
  public void setPhase(Phase phase) {
    this.phase = phase;
  }

  /**
   * Retrieves the number of remaining steps the current player can take during their turn.
   *
   * @return The number of steps left for the current player to move.
   */
  public int getStepsLeft() {
    return stepsLeft;
  }

  /**
   * Sets the number of steps remaining for the current player to move.
   *
   * @param stepsLeft The number of steps remaining for the player's movement.
   */
  public void setStepsLeft(int stepsLeft) {
    this.stepsLeft = stepsLeft;
  }

  /**
   * Retrieves the solution suspect for the game.
   *
   * @return The {@link Suspect} object representing the solution suspect.
   */
  public Suspect getSolutionSuspect() {
    return solutionSuspect;
  }

  /**
   * Retrieves the solution weapon for the game.
   *
   * @return The {@link Weapon} object representing the solution weapon.
   */
  public Weapon getSolutionWeapon() {
    return solutionWeapon;
  }

  /**
   * Retrieves the solution room for the game.
   *
   * @return The {@link Room} object representing the solution room.
   */
  public Room getSolutionRoom() {
    return solutionRoom;
  }

  /**
   * Sets the solution for the game, including the suspect, weapon, and room.
   *
   * @param suspect The {@link Suspect} object representing the solution suspect.
   * @param weapon  The {@link Weapon} object representing the solution weapon.
   * @param room    The {@link Room} object representing the solution room.
   */
  public void setSolution(Suspect suspect, Weapon weapon, Room room) {
    this.solutionSuspect = suspect;
    this.solutionWeapon = weapon;
    this.solutionRoom = room;
    logger.info("Solution loaded: {} with {} in {}",
        solutionSuspect.getName(), solutionWeapon.getName(), solutionRoom.getName());
  }
}
