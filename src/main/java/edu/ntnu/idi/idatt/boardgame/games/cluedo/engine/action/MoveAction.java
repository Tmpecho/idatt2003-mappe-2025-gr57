package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.GridPos;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;

/**
 * Attempts to move the current player one square (or into / out of a room). Legal-move checking is
 * delegated to the board.
 */
public final class MoveAction implements Action {

  private final CluedoController controller;
  private final GridPos target;

  /**
   * Constructs a new MoveAction to move the current player to the specified target position.
   *
   * @param controller the CluedoController managing the game state and player actions
   * @param target the target GridPos to which the player should be moved
   */
  public MoveAction(CluedoController controller, GridPos target) {
    this.controller = controller;
    this.target = target;
  }

  @Override
  public void execute() {
    controller.movePlayerTo(target); // controller delegates to board & updates phase
  }
}
