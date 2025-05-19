package edu.ntnu.idi.idatt.boardgame.games.snakesandladders.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Player;
import edu.ntnu.idi.idatt.boardgame.core.domain.player.Position;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;

public final class RollAction<P extends Position> implements Action {
  private final GameBoard<P> gameBoard;
  private final Player<P> player;
  private final Dice dice;

  public RollAction(GameBoard<P> gameBoard, Player<P> player, Dice dice) {
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