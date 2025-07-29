package cn.kduck.core.exception;

/**
 * 根据错误编码和data对象，通过自定义接口进行自定义处理
 */
public class KduckException extends RuntimeException{

    private String errorCode;
    private String userMessage;
    private boolean sendNotice = false;
    private Object data;

    public KduckException() {
    }

    public KduckException(String message) {
        super(message);
    }

    public KduckException(String message, Throwable cause) {
        super(message, cause);
    }

    public KduckException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public boolean isSendNotice() {
        return sendNotice;
    }

    public void setSendNotice(boolean sendNotice) {
        this.sendNotice = sendNotice;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
