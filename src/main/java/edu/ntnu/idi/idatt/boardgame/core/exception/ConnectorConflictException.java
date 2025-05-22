package edu.ntnu.idi.idatt.boardgame.core.exception;

public class ConnectorConflictException extends RuntimeException {
    public ConnectorConflictException(String message) {
        super(message);
    }

    public ConnectorConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
