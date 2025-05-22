package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

/** A “playing card” in Cluedo: exactly one of Suspect, Weapon or Room. */
public sealed interface Card permits Suspect, Weapon, Room {

  /**
   * Retrieves the name associated with this card.
   *
   * @return the name of this card as a String
   */
  String getName();
}
