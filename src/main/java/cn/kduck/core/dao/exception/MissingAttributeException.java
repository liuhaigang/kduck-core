package cn.kduck.core.dao.exception;

public class MissingAttributeException extends RuntimeException{

    public MissingAttributeException() {
    }

    public MissingAttributeException(String message) {
        super(message);
    }

    public MissingAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingAttributeException(Throwable cause) {
        super(cause);
    }
}
