package edu.ntnu.idi.idatt.boardgame.common.domain.board;

import edu.ntnu.idi.idatt.boardgame.common.player.Player;
import java.util.List;

/**
 * A generic Tile interface for any board game that is laid out in discrete tiles (spaces).
 * In Snakes and Ladders, movement is numeric. In Cluedo, movement is to adjacent tiles.
 */
public interface Tile {
	/**
	 * Returns a list of adjacent tiles.
	 * - In Snakes and Ladders, this might be just the “next” tile (plus any jump if there’s a snake or ladder).
	 * - In Cluedo, this might be up to 4 neighboring hallway tiles or a door tile leading into a room.
	 */
	List<Tile> getAdjacentTiles();

	/**
	 * Called when a player arrives on this tile.
	 */
	void addPlayer(Player player);

	/**
	 * Called when a player leaves this tile.
	 */
	void removePlayer(Player player);

	/**
	 * Whether a player is allowed to enter or exit this tile (e.g., a wall or locked room might disallow entry).
	 */
	boolean canTraverse(Player player);

	/**
	 * An optional identifier for debugging or referencing a tile number, name, etc.
	 */
	String getIdentifier();
}