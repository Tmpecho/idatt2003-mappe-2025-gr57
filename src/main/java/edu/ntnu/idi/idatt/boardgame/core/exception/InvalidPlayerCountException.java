package edu.ntnu.idi.idatt.boardgame.core.exception;

public class InvalidPlayerCountException extends RuntimeException {
    public InvalidPlayerCountException(String message) {
        super(message);
    }

    public InvalidPlayerCountException(String message, Throwable cause) {
        super(message, cause);
    }
}
