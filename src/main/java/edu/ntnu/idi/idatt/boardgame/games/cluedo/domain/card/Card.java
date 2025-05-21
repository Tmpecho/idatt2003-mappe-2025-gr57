package edu.ntnu.idi.idatt.boardgame.games.cluedo.domain.card;

/**
 * Represents a single card in the Cluedo game.
 * Each card has a name (e.g., "Miss Scarlett", "Kitchen", "Rope") and a type.
 *
 * @param name The name of the card.
 * @param type The {@link CardType} of the card (SUSPECT, WEAPON, or ROOM).
 */
public record Card(String name, CardType type) {}
