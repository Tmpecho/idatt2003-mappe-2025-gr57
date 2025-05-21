package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.AbstractCluedoTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.CardType;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Cards;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import edu.ntnu.idi.idatt.boardgame.ui.util.LoggingNotification;

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
 * Controller for the Cluedo game. Manages game flow, player turns,
 * actions like moving, suggesting, and accusing.
 */
public final class CluedoController extends GameController<GridPos> {
  private final CluedoBoard boardModel;
  private final int numberOfPlayers;
  private int stepsLeft = 0;
  private List<Card> deck = new ArrayList<>();
  /** The three cards (suspect, weapon, room) that form the solution to the mystery. */
  private final Card[] solution = new Card[3];
  private final Random rng = new SecureRandom();
  /** List of available suspects, used for player creation. */
  private final List<Suspect> suspects = List.of(Suspect.values());

  /**
   * Constructs a CluedoController.
   *
   * @param numberOfPlayers The number of players in the game (2-6).
   * @throws IllegalArgumentException if numberOfPlayers is not between 2 and 6.
   */
  public CluedoController(int numberOfPlayers) {
    super(new CluedoBoard(), new Dice(2)); // Cluedo uses 2 dice
    this.boardModel = (CluedoBoard) this.gameBoard;

    if (numberOfPlayers < 2 || numberOfPlayers > 6) {
      throw new IllegalArgumentException("Cluedo requires 2 to 6 players.");
    }

    this.numberOfPlayers = numberOfPlayers;

    createCards(); // Create and shuffle deck, select solution

    super.initialize(numberOfPlayers); // Create players and place them

    distributeCards(); // Distribute remaining cards to players

    notifyObservers("Game initialised. " + currentPlayer.getName() + " starts.");
  }

  @Override
  protected Map<Integer, Player<GridPos>> createPlayers(int n) {
    Map<Integer, Player<GridPos>> map = new HashMap<>();
    IntStream.rangeClosed(1, n)
            .forEach(
                    i -> {
                      Suspect suspect = suspects.get(i - 1); // Assign suspects in order
                      // Start position is set by CluedoBoard.addPlayersToStart
                      CluedoPlayer player =
                              new CluedoPlayer(i, suspect.displayName(), suspect.colour(), new GridPos(0,0)); // Temp start pos
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
    // Simple clockwise turn order
    int currentId = currentPlayer.getId();
    int nextId = (currentId % numberOfPlayers) + 1;
    return players.get(nextId);
  }

  @Override
  public void saveGameState(String filePath) {
    // TODO: Implement Cluedo-specific game state saving
    System.out.println("Cluedo save game state not implemented yet for path: " + filePath);
    LoggingNotification.warn("Not implemented", "Saving Cluedo game state is not yet supported.");
    // Need a CluedoGameStateDTO, Mapper, and Repository similar to SnL
  }

  @Override
  public void loadGameState(String filePath) {
    // TODO: Implement Cluedo-specific game state loading
    System.out.println("Cluedo load game state not implemented yet from path: " + filePath);
    LoggingNotification.warn("Not implemented", "Loading Cluedo game state is not yet supported.");
  }

  /** How many steps remain this turn for the current player.
   * @return The number of steps left.
   */
  public int getStepsLeft() {
    return stepsLeft;
  }

  /** Roll two dice and begin a move phase. Disables further rolls until this turn completes or player enters a room. */
  public void rollDiceAndMove() {
    if (stepsLeft > 0) {
      notifyObservers(currentPlayer.getName() + " still has " + stepsLeft + " steps to move.");
      return;
    }
    int roll = dice.roll();
    this.stepsLeft = roll;
    notifyObservers(
            currentPlayer.getName()
                    + " rolled a "
                    + roll
                    + ". You have "
                    + stepsLeft
                    + " steps remaining. Click on an adjacent tile to move.");
  }

  /**
   * Try to move the current player to the clicked target {@link GridPos}.
   * Movement is allowed between adjacent corridor tiles, or between a corridor and an adjacent room
   * if a valid door exists. Entering a room typically ends the movement phase for the turn.
   * Each step decrements {@link #stepsLeft}.
   *
   * @param target The target {@link GridPos} to move to.
   */
  public void movePlayerTo(GridPos target) {
    if (stepsLeft <= 0) {
      notifyObservers(currentPlayer.getName() + " has no steps left. Roll dice or end turn if in a room.");
      return;
    }

    GridPos here = currentPlayer.getPosition();
    AbstractCluedoTile fromTileModel = boardModel.getTileAtPosition(here);
    AbstractCluedoTile toTileModel = boardModel.getTileAtPosition(target);

    if (fromTileModel == null || toTileModel == null) {
      notifyObservers("Invalid move: Start or end tile does not exist.");
      return;
    }

    // Basic adjacency check (Manhattan distance of 1)
    boolean adjacent = Math.abs(here.row() - target.row()) + Math.abs(here.col() - target.col()) == 1;
    if (!adjacent) {
      notifyObservers("Invalid move: Target tile is not adjacent.");
      return;
    }

    boolean isValidMove = false;

    // Case 1: Corridor to Corridor
    if (fromTileModel instanceof CorridorTile && toTileModel instanceof CorridorTile && toTileModel.isWalkable()) {
      isValidMove = true;
    }
    // Case 2: Corridor to Room (entering a room)
    else if (fromTileModel instanceof CorridorTile && toTileModel instanceof RoomTile roomTarget) {
      if (roomTarget.canEnterFrom(here.row(), here.col())) {
        isValidMove = true;
      } else {
        notifyObservers("Invalid move: Cannot enter room " + roomTarget.getRoomName() + " from this corridor tile. No door.");
      }
    }
    // Case 3: Room to Corridor (exiting a room)
    else if (fromTileModel instanceof RoomTile roomOrigin && toTileModel instanceof CorridorTile && toTileModel.isWalkable()) {
      if (roomOrigin.canExitTo(target.row(), target.col())) {
        isValidMove = true;
      } else {
        notifyObservers("Invalid move: Cannot exit room " + roomOrigin.getRoomName() + " to this corridor tile. No door.");
      }
    }
    // Case 4: Moving within the same room (generally not allowed as a "step", but for teleportation/placement)
    // This method is for step-by-step movement.
    else if (fromTileModel instanceof RoomTile && toTileModel instanceof RoomTile && fromTileModel == toTileModel) {
      // Moving within the same room doesn't consume a step in classic Cluedo.
      // This logic path shouldn't be hit for normal movement.
      // If it's for a special action, that action should handle it.
      notifyObservers("Already in the room. No steps consumed for internal movement.");
      return; // Or handle differently based on game rules for "moving within a room"
    }

    if (!isValidMove) {
      if (! (toTileModel instanceof RoomTile) && !toTileModel.isWalkable()){
        notifyObservers("Invalid move: Target tile " + target + " is not walkable.");
      } else if (!isValidMove) { // General fallback if no specific reason given
        notifyObservers("Invalid move to " + target + " from " + here + ".");
      }
      return;
    }

    // Perform the move
    boardModel.movePlayer(currentPlayer, target); // Uses board's internal logic which calls setPlayerPosition
    // setPlayerPosition handles tile.removePlayer and tile.addPlayer

    stepsLeft--;

    String message = currentPlayer.getName() + " moved to " + target + ". ";
    if (toTileModel instanceof RoomTile) {
      stepsLeft = 0; // Entering a room ends movement phase
      message += "Entered " + ((RoomTile) toTileModel).getRoomName() + ". Turn movement ends. You can make a suggestion.";
      // TODO: Enable suggestion button if applicable
    } else {
      message += stepsLeft + " steps left.";
    }
    notifyObservers(message);


    if (stepsLeft == 0) {
      if (!(toTileModel instanceof RoomTile)) { // If ended turn in corridor
        endTurn();
      }
      // If ended in a room, player can still make suggestion/accusation before turn ends.
      // endTurn() might be called after suggestion/accusation.
    }
  }

  /**
   * Gets the current player.
   * @return The {@link Player} whose turn it is.
   */
  public Player<GridPos> getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Allows the current player to make a suggestion.
   * This is typically done when the player is in a room.
   * The player suggests a suspect, a weapon, and the current room.
   * Other players then attempt to disprove the suggestion.
   */
  public void makeSuggestion() {
    // TODO: Implement suggestion logic
    // 1. Check if player is in a room.
    // 2. Prompt for suspect and weapon. Room is current room.
    // 3. Move the suggested suspect and weapon token to the current room.
    // 4. Other players (clockwise) try to show a card.
    // 5. Update observers.
    AbstractCluedoTile currentTile = boardModel.getTileAtPosition(currentPlayer.getPosition());
    if (!(currentTile instanceof RoomTile)) {
      notifyObservers(currentPlayer.getName() + " must be in a room to make a suggestion.");
      return;
    }
    notifyObservers(currentPlayer.getName() + " is making a suggestion in " + ((RoomTile)currentTile).getRoomName() + ". (Suggestion UI TBD)");
    // For now, just end turn after "suggestion"
    endTurn();
  }

  /**
   * Allows the current player to make an accusation.
   * This can usually be done at any time on the player's turn.
   * If the accusation is correct, the player wins. If incorrect, the player may be out of the game
   * or unable to make further accusations, depending on house rules.
   */
  public void makeAccusation() {
    // TODO: Implement accusation logic
    // 1. Prompt for suspect, weapon, and room.
    // 2. Compare with `solution` cards.
    // 3. If correct, game over, current player wins. Call onGameFinish().
    // 4. If incorrect, handle consequences (e.g., player can no longer win/move).
    // 5. Update observers.
    notifyObservers(currentPlayer.getName() + " is making an accusation. (Accusation UI and logic TBD)");
    // For now, assume accusation is made, and game continues / ends turn.
    // if (isGameOver()) onGameFinish(); else endTurn(); // This depends on accusation result
    endTurn(); // Placeholder
  }

  /** Called when this player’s movement or action phase finishes. Advances turn. */
  private void endTurn() {
    Player<GridPos> next = getNextPlayer();
    currentPlayer = next;
    stepsLeft = 0; // Reset steps for the new player
    notifyObservers("Turn over. It is now " + next.getName() + "'s turn. Roll dice to move.");
    // TODO: Disable suggestion/accusation buttons, enable roll dice button
  }

  /** Build a complete shuffled deck and pick the three solution cards. */
  private void createCards() {
    deck = Cards.shuffledDeck(rng);

    // Select one card of each type for the solution, removing them from the deck
    solution[0] = drawCardFromDeck(CardType.SUSPECT);
    solution[1] = drawCardFromDeck(CardType.WEAPON);
    solution[2] = drawCardFromDeck(CardType.ROOM);

    // For debugging:
    // System.out.println("Solution: " + solution[0].name() + ", " + solution[1].name() + ", " + solution[2].name());
  }

  // Helper to draw and remove the first card of a specific type from the main deck.
  private Card drawCardFromDeck(CardType wanted) {
    for (Iterator<Card> iterator = deck.iterator(); iterator.hasNext(); ) {
      Card card = iterator.next();
      if (card.type() == wanted) {
        iterator.remove(); // Remove from deck
        return card;
      }
    }
    throw new IllegalStateException("Could not find a card of type " + wanted + " for the solution.");
  }

  /** Deal the remaining (non-solution) cards from the deck to players, one at a time, clockwise. */
  private void distributeCards() {
    // Ensure players are CluedoPlayers and sort by ID for consistent distribution
    List<CluedoPlayer> cluedoPlayers =
            players.values().stream()
                    .filter(p -> p instanceof CluedoPlayer)
                    .map(p -> (CluedoPlayer) p)
                    .sorted(Comparator.comparingInt(Player::getId)) // Deals cards starting with Player 1
                    .toList();

    if (cluedoPlayers.isEmpty() && !players.isEmpty()) {
      throw new IllegalStateException("Players map does not contain CluedoPlayer instances.");
    }
    if (cluedoPlayers.isEmpty() && players.isEmpty()) {
      // This might happen if initialize was not called or no players were created.
      System.err.println("Warning: No players to distribute cards to.");
      return;
    }


    int playerIndex = 0;
    while (!deck.isEmpty()) {
      Card cardToDeal = deck.remove(0); // Take card from the top of the shuffled deck
      cluedoPlayers.get(playerIndex).addCard(cardToDeal);
      playerIndex = (playerIndex + 1) % cluedoPlayers.size(); // Move to next player
    }
  }
}
