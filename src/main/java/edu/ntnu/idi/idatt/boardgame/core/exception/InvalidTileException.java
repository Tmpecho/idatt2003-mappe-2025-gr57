package edu.ntnu.idi.idatt.boardgame.core.exception;

public class InvalidTileException extends RuntimeException {
    public InvalidTileException(String message) {
        super(message);
    }

    public InvalidTileException(String message, Throwable cause) {
        super(message, cause);
    }
}
