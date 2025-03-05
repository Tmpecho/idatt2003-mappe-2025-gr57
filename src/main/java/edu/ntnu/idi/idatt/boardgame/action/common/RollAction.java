package edu.ntnu.idi.idatt.boardgame.action.common;

import edu.ntnu.idi.idatt.boardgame.domain.common.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.common.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.games.snakesAndLadders.player.SnakesAndLaddersPlayer;

public class RollAction implements Action {
  private final GameBoard gameBoard;
  private final SnakesAndLaddersPlayer player;
  private final Dice dice;

  public RollAction(GameBoard gameBoard, SnakesAndLaddersPlayer player, Dice dice) {
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
