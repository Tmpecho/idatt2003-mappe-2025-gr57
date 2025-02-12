package edu.ntnu.idi.idatt.boardgame;

import edu.ntnu.idi.idatt.boardgame.model.Dice;
import edu.ntnu.idi.idatt.boardgame.model.GameBoard;
import edu.ntnu.idi.idatt.boardgame.model.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class Game {
  private final GameBoard gameBoard;
  private final Dice dice;
  private final Button rollDiceButton;

  private final int numberOfPlayers = 2;
  private final List<Color> playerColors =
      List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE);
  private final Map<Integer, Player> players;
  private Player currentPlayer;
  private boolean gameOver = false;

  public Game() {
    this.dice = new Dice(2);
    this.players = createPlayers();
    this.gameBoard = new GameBoard();
    this.rollDiceButton = new Button("Roll dice");

    gameBoard.addPlayersToStart(players);
    this.currentPlayer = players.get(1);

    setupRollDiceButton();
  }

  /**
   * Sets up the event handler for the roll dice button. When pressed, the dice are rolled, the
   * current player's position is updated on the board, and the turn passes to the next player.
   */
  private void setupRollDiceButton() {
    rollDiceButton.setOnAction(
        e -> {
          if (!gameOver) {
            int roll = dice.roll();
            gameBoard.incrementPlayerPosition(currentPlayer, roll);

            if (currentPlayer.getPosition() == GameBoard.getBoardSize()) {
              gameOver = true;
              System.out.println("Player " + currentPlayer.getId() + " wins!");
              rollDiceButton.setDisable(true);
            } else {
              currentPlayer = getNextPlayer();
            }
          }
        });
  }

  /**
   * Returns the next player in turn.
   *
   * @return the next Player
   */
  private Player getNextPlayer() {
    return players.get((currentPlayer.getId() % numberOfPlayers) + 1);
  }

  /**
   * Creates the players and returns them in a map keyed by their IDs.
   *
   * @return a map of players
   */
  private Map<Integer, Player> createPlayers() {
    Map<Integer, Player> players = new HashMap<>();
    IntStream.rangeClosed(1, numberOfPlayers)
        .forEach(
            playerId -> {
              Player player = new Player(playerId, playerColors.get(playerId - 1));
              players.put(playerId, player);
            });
    return players;
  }

  public GameBoard getGameBoard() {
    return gameBoard;
  }

  public Button getRollDiceButton() {
    return rollDiceButton;
  }
}
