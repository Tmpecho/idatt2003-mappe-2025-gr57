package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.LinearPos;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.games.snakesandladders.domain.board.SnLBoard;

public final class RollAction implements Action {
  private final SnLBoard gameBoard;
  private final Player<LinearPos> player;
  private final Dice dice;

  public RollAction(SnLBoard gameBoard, Player<LinearPos> player, Dice dice) {
    this.gameBoard = gameBoard;
    this.player = player;
    this.dice = dice;
  }

  @Override
  public void execute() {
    int roll = dice.roll();
    gameBoard.incrementPlayerPosition(player, roll);
  }
}