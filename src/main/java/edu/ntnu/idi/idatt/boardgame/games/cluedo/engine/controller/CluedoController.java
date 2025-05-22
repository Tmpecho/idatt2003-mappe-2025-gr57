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
   * @param playerDetailsList List of player setup details.
   * @param repo              Repository for saving and loading game state.
   */
  public CluedoController(List<PlayerSetupDetails> playerDetailsList,
      GameStateRepository<CluedoGameStateDto> repo) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;
    this.repo = Objects.requireNonNull(repo);

    if (playerDetailsList == null || playerDetailsList.size() < 2 || playerDetailsList.size() > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }

    initializeGame(playerDetailsList);

    pickSolution();
    dealRemainingCards();

    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Deprecated
  public CluedoController(int numberOfPlayers, GameStateRepository<CluedoGameStateDto> repo) {
    super(new CluedoBoard(), new Dice(2));
    this.boardModel = (CluedoBoard) this.gameBoard;
    this.repo = Objects.requireNonNull(repo);

    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }
    initialize(numberOfPlayers);

    pickSolution();
    dealRemainingCards();
    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Override
  protected Map<Integer, Player<GridPos>> setupPlayers(List<PlayerSetupDetails> playerDetailsList) {
    LinkedHashMap<Integer, Player<GridPos>> newPlayersMap = new LinkedHashMap<>();
    this.turnOrder.clear();
    AtomicInteger playerIdCounter = new AtomicInteger(1);

    for (PlayerSetupDetails detail : playerDetailsList) {
      int id = playerIdCounter.getAndIncrement();
      String name = detail.name();
      Suspect suspect = detail.suspectIfCluedo().orElseThrow(
          () -> new IllegalArgumentException("Suspect details missing for Cluedo player: " + name)
      );
      CluedoPlayer player = new CluedoPlayer(id, name, suspect.colour(),
          new GridPos(0, 0));
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

  @Deprecated
  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int n) {
    final List<Suspect> defaultSuspectOrder = List.of(Suspect.values());
    LinkedHashMap<Integer, Player<GridPos>> map = new LinkedHashMap<>();
    this.turnOrder.clear();

    for (int i = 0; i < n; i++) { // Use 0-based index for defaultSuspectOrder
      int playerId = i + 1;
      Suspect s = defaultSuspectOrder.get(i % defaultSuspectOrder.size());
      CluedoPlayer p = new CluedoPlayer(playerId, s.getName(), s.colour(), new GridPos(0, 0));
      map.put(playerId, p);
      this.turnOrder.add(p);
    }
    return map;
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
        CluedoPlayer player = new CluedoPlayer(ps.id, suspect.getName(), playerColor,
            new GridPos(ps.row, ps.col));
        loadedPlayers.put(ps.id, player);
        this.turnOrder.add(player);
      }
      this.players = loadedPlayers;

      CluedoMapper.apply(dto, this);

      if (this.currentPlayer != null && this.turnOrder.contains(this.currentPlayer)) {
        this.currentIndex = this.turnOrder.indexOf(this.currentPlayer);
      } else if (!this.turnOrder.isEmpty()) {
        logger.warn(
            "Loaded currentPlayer not in turnOrder or turnOrder empty. Resetting currentIndex.");
        this.currentIndex = 0;
        this.currentPlayer = this.turnOrder.get(this.currentIndex);
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
          message += " Current turn: " + currentPlayer.getName() + ".";
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

    Player<GridPos> suggestedPlayer = null;
    for (Player<GridPos> p : players.values()) {
      if (p instanceof CluedoPlayer cp && cp.getName().equals(suggestedSuspect.getName())) {
        suggestedPlayer = p;
        break;
      }
    }
    if (suggestedPlayer != null && suggestedPlayer != currentPlayer) {
      boardModel.setPlayerPosition(suggestedPlayer, currentPlayer.getPosition());
      LoggingNotification.info("Player Moved",
          suggestedSuspect.getName() + " has been moved to the " + suggestedRoom.getName());
    }

    int totalPlayersInOrder = turnOrder.size();
    boolean disproved = false;
    for (int i = 1; i < totalPlayersInOrder; i++) {
      int respondentIdx = (this.currentIndex + i) % totalPlayersInOrder;
      CluedoPlayer respondent = (CluedoPlayer) turnOrder.get(respondentIdx);

      if (respondent == currentPlayer) {
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
          currentIndex = turnOrder.size() - 1;
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
      onGameFinish();
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
          "setCurrentPlayer called with a player not found in turnOrder. "
              + "This might occur during load if turnOrder isn't perfectly aligned yet.");
      if (!this.turnOrder.contains(currentPlayer)) {
      }
      if (this.turnOrder.contains(currentPlayer)) {
        this.currentIndex = this.turnOrder.indexOf(currentPlayer);
      } else if (!this.turnOrder.isEmpty()) {
        this.currentIndex = 0;
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
}
