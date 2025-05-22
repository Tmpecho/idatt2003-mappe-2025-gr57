package edu.ntnu.idi.idatt.boardgame.core.exception;

/**
 * Exception thrown when an unknown player color is encountered. This exception is a runtime
 * exception and does not require explicit handling.
 */
public class InvalidTileException extends RuntimeException {

  /**
   * Constructs a new InvalidTileException with the specified detail message.
   *
   * @param message the detail message
   */
  public InvalidTileException(String message) {
    super(message);
  }

  /**
   * Constructs a new InvalidTileException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public InvalidTileException(String message, Throwable cause) {
    super(message, cause);
  }
}
