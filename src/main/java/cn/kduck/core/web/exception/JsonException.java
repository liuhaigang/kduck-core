package cn.kduck.core.web.exception;

public class JsonException extends Exception{

    private final int code;
    private final Object data;

    public JsonException(String message){
        this(null,-1,message);
    }

    public JsonException(Object data,String message){
        this(data,-1,message);
    }

    public JsonException(int code,String message){
        this(null,code,message);
    }

    public JsonException(Object data, int code, String message){
        super(message);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
