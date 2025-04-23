package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;

public class RollAction implements Action {
  private final GameBoard gameBoard;
  private final Player player;
  private final Dice dice;

  public RollAction(GameBoard gameBoard, Player player, Dice dice) {
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