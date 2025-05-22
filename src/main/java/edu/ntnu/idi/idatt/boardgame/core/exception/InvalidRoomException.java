package edu.ntnu.idi.idatt.boardgame.core.exception;

public class InvalidRoomException extends RuntimeException {
    public InvalidRoomException(String message) {
        super(message);
    }

    public InvalidRoomException(String message, Throwable cause) {
        super(message, cause);
    }
}
