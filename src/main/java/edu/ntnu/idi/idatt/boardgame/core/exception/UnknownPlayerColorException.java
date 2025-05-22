package edu.ntnu.idi.idatt.boardgame.core.exception;

/**
 * Exception thrown when an unknown player color is encountered.
 */
public class UnknownPlayerColorException extends RuntimeException {

  /**
   * Constructs a new UnknownPlayerColorException with the specified detail message.
   *
   * @param message the detail message
   */
  public UnknownPlayerColorException(String message) {
    super(message);
  }

  /**
   * Constructs a new UnknownPlayerColorException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public UnknownPlayerColorException(String message, Throwable cause) {
    super(message, cause);
  }

}
