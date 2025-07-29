package cn.kduck.core.dao.exception;

public class NoFieldMatchedException extends RuntimeException{
    public NoFieldMatchedException() {
    }

    public NoFieldMatchedException(String message) {
        super(message);
    }

    public NoFieldMatchedException(String message, Throwable cause) {
        super(message, cause);
    }
}
