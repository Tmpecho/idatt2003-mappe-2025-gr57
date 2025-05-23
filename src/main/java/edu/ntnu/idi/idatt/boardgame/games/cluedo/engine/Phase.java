package edu.ntnu.idi.idatt.boardgame.games.cluedo.engine;

/**
 * Represents the different phases of a Cluedo game. The game's progression is divided into these
 * phases, where each phase dictates the allowed actions for players during their turn.
 */
public enum Phase {
  WAIT_ROLL,
  MOVING,
  IN_ROOM,
  TURN_OVER
}
