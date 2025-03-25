package edu.ntnu.idi.idatt.boardgame.games.snakesAndLadders.action;

import edu.ntnu.idi.idatt.boardgame.common.action.Action;
import edu.ntnu.idi.idatt.boardgame.common.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.common.player.Player;

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
  public String execute() {
    int roll = dice.roll();
    return gameBoard.incrementPlayerPosition(player, roll);
  }
}