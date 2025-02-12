package cn.kduck.core.exception;

public class NonUniqueAttributeException extends RuntimeException{

    public NonUniqueAttributeException() {
    }

    public NonUniqueAttributeException(String message) {
        super(message);
    }

    public NonUniqueAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}
