package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnlBoard;

/** Represents the action of a player rolling dice and moving on the Snakes and Ladders board. */
public final class RollAction implements Action {

  private final SnlBoard gameBoard;
  private final Player<LinearPos> player;
  private final Dice dice;

  /**
   * Constructs a RollAction.
   *
   * @param gameBoard The {@link SnlBoard} on which the action takes place.
   * @param player The {@link Player} performing the roll and move.
   * @param dice The {@link Dice} to be rolled.
   */
  public RollAction(SnlBoard gameBoard, Player<LinearPos> player, Dice dice) {
    this.gameBoard = gameBoard;
    this.player = player;
    this.dice = dice;
  }

  /**
   * Executes the roll and move action. The dice are rolled, and the player's position on the game
   * board is updated accordingly. This includes handling snakes and ladders.
   */
  @Override
  public void execute() {
    int roll = dice.roll();
    gameBoard.incrementPlayerPosition(player, roll);
  }
}
