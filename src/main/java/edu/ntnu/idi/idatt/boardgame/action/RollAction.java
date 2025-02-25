package edu.ntnu.idi.idatt.boardgame.action;

import edu.ntnu.idi.idatt.boardgame.domain.board.GameBoard;
import edu.ntnu.idi.idatt.boardgame.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.domain.player.Player;

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