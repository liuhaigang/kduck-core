package cn.kduck.core.dao.exception;

/**
 * LiuHG
 */
public class PrimaryKeyNotFoundException extends RuntimeException{
    public PrimaryKeyNotFoundException() {
    }

    public PrimaryKeyNotFoundException(String message) {
        super(message);
    }

    public PrimaryKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrimaryKeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
