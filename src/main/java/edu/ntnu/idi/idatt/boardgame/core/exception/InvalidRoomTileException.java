package edu.ntnu.idi.idatt.boardgame.core.exception;

/**
 * Exception thrown when an unknown player color is encountered. This exception is a runtime
 * exception and does not require explicit handling.
 */
public class InvalidRoomTileException extends RuntimeException {

  /**
   * Constructs a new InvalidRoomTileException with the specified detail message.
   *
   * @param message the detail message
   */
  public InvalidRoomTileException(String message) {
    super(message);
  }

  /**
   * Constructs a new InvalidRoomTileException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public InvalidRoomTileException(String message, Throwable cause) {
    super(message, cause);
  }
}
