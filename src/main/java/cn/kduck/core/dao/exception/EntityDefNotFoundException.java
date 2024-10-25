package cn.kduck.core.dao.exception;

public class EntityDefNotFoundException extends RuntimeException{

    public EntityDefNotFoundException() {
        super();
    }

    public EntityDefNotFoundException(String message) {
        super(message);
    }

    public EntityDefNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
