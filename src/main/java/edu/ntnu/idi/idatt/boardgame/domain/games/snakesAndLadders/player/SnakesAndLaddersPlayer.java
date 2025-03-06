package edu.ntnu.idi.idatt.boardgame.domain.games.snakesAndLadders.player;

import edu.ntnu.idi.idatt.boardgame.domain.common.board.GameBoard;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class SnakesAndLaddersPlayer extends edu.ntnu.idi.idatt.boardgame.domain.common.player.Player {
	private int position;

	/**
	 * Creates a new player with the given id and color.
	 *
	 * @param id    the id of the player
	 * @param color the color of the player
	 */
	public SnakesAndLaddersPlayer(int id, Paint color) {
		super(id, color);
		this.position = 1;
		Circle icon = new Circle(7);
		icon.setFill(color);
	}

	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * Sets the position of the player.
	 *
	 * @param position the new position of the player
	 */
	@Override
	public void setPosition(int position) {
		if (position < 1 || position > GameBoard.getBoardSize()) {
			throw new IllegalArgumentException(
					"Position must be between 1 and the furthest position on the board.");
		}
		this.position = position;
	}

	/**
	 * Increments the position of the player by the given increment.
	 *
	 * @param increment the amount to increment the position by
	 * @return the new position of the player
	 */
	public int incrementPosition(int increment) {
		int newPosition = position + increment;
		if (newPosition > GameBoard.getBoardSize()) {
			// bounce back
			newPosition = GameBoard.getBoardSize() - (newPosition - GameBoard.getBoardSize());
		}
		return newPosition;
	}
}
