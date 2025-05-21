package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

/** A “playing card” in Cluedo: exactly one of Suspect, Weapon or Room. */
public sealed interface Card permits Suspect, Weapon, Room {}
