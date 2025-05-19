package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.core.engine.controller.GameController;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CluedoBoard;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.CorridorTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.board.RoomTile;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Card;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.CardType;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Cards;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.player.CluedoPlayer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
  private final List<Card> deck = new ArrayList<>();
  private final Card[] solution = new Card[3];
  private final Random rng = new SecureRandom();

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
              PlayerColor colour = playerColors.get(i - 1);
              String name = playerNames.get(colour);
              CluedoPlayer player = new CluedoPlayer(i, name, colour, new GridPos(0, 0));
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

  /** Build a complete shuffled deck and pick the three solution cards. */
  private void createCards() {
    deck.clear();
    Arrays.stream(Cards.getPeople()).map(s -> new Card(s, CardType.SUSPECT)).forEach(deck::add);
    Arrays.stream(Cards.getWeapons()).map(s -> new Card(s, CardType.WEAPON)).forEach(deck::add);
    Arrays.stream(Cards.getRooms()).map(s -> new Card(s, CardType.ROOM)).forEach(deck::add);
    Collections.shuffle(deck, rng);

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
