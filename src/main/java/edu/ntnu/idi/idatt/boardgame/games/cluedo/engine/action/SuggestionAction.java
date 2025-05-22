package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.action;

import edu.ntnu.idi.idatt.boardgame.core.engine.action.Action;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Room;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Weapon;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.engine.controller.CluedoController;

/**
 * Represents an action where the current player makes a suggestion in the game of Cluedo.
 */
public final class SuggestionAction implements Action {

  private final CluedoController cluedoController;
  private final Suspect suspect;
  private final Weapon weapon;
  private final Room room;

  public SuggestionAction(
      CluedoController cluedoController, Suspect suspect, Weapon weapon, Room room) {
    this.cluedoController = cluedoController;
    this.suspect = suspect;
    this.weapon = weapon;
    this.room = room;
  }

  @Override
  public void execute() {
    cluedoController.makeSuggestion(suspect, weapon, room);
  }
}
