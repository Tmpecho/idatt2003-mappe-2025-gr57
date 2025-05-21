package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.domain.dice.Dice;
import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;

/**
 * Rolls two dice and starts a MOVE phase. Must be executed while the controller is in WAIT_ROLL.
 */
public final class RollAction implements Action {

  private final CluedoController controller;
  private final Dice dice;

  /**
   * Constructs a new RollAction that will roll the specified dice and start a MOVE phase using the
   * given controller.
   *
   * @param controller the CluedoController managing the game state and player actions
   * @param dice       the Dice object used to perform the roll
   */
  public RollAction(CluedoController controller, Dice dice) {
    this.controller = controller;
    this.dice = dice;
  }

  @Override
  public void execute() {
    if (!controller.isWaitingForRoll()) {
      return; // ignore illegal click
    }

    int roll = dice.roll();
    controller.beginMovePhase(roll); // sets stepsLeft & phase
  }
}
