package edu.ntnu.idi.idatt.boardgame.ui.dto;

import edu.ntnu.idi.idatt.boardgame.core.domain.player.PlayerColor;
import edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card.Suspect;
import java.util.Optional;

/**
 * Data Transfer Object for carrying player setup information from the UI to the game initialization
 * logic.
 *
 * @param name            The name of the player.
 * @param color           The chosen PlayerColor (primarily for games like Snakes and Ladders).
 * @param suspectIfCluedo The chosen Suspect if the game is Cluedo, otherwise empty. For Cluedo, the
 *                        color will be derived from the suspect.
 */
public record PlayerSetupDetails(String name, Optional<PlayerColor> color,
                                 Optional<Suspect> suspectIfCluedo) {

}
