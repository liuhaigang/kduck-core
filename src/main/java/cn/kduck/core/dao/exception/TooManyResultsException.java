package cn.kduck.core.dao.exception;

public class TooManyResultsException extends RuntimeException{
    public TooManyResultsException() {
    }

    public TooManyResultsException(String message) {
        super(message);
    }

    public TooManyResultsException(String message, Throwable cause) {
        super(message, cause);
    }
}
