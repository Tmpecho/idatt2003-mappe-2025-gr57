package edu.ntnu.idi.idatt.boardgame.core.exception;

public class InvalidRoomTileException extends RuntimeException {

  public InvalidRoomTileException(String message) {
    super(message);
  }

  public InvalidRoomTileException(String message, Throwable cause) {
    super(message, cause);
  }
}
