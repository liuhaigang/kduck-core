package cn.kduck.core.service.exception;

/**
 * LiuHG
 */
public class AttributeNotExistException extends RuntimeException{
    public AttributeNotExistException() {
    }

    public AttributeNotExistException(String message) {
        super(message);
    }

    public AttributeNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttributeNotExistException(Throwable cause) {
        super(cause);
    }
}
