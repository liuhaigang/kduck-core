package cn.kduck.core.service.exception;

/**
 * @author LiuHG
 */
public class QueryNotFoundException extends RuntimeException{

    public QueryNotFoundException() {
    }

    public QueryNotFoundException(String message) {
        super(message);
    }

    public QueryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryNotFoundException(Throwable cause) {
        super(cause);
    }
}
