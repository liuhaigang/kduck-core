package cn.kduck.core.web.json;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一的接口返回对象，以Json形式呈现，并包含data、code、message属性。</p>
 * data用于返回请求的数据，code用于返回请求的状态；该状态与HTTP的状态码不同，该编码仅在成功请求时，用于请求操作的业务状态的标识，成功返回{@link JsonObject#SUCCESS}，
 * 无任何特征业务状态的失败返回{@link JsonObject#FAIL}；message返回当前操作需要提示的文本信息，一般与code配对使用，表示在非成功状态时具体错误的内容。
 * @author LiuHG
 * @see JsonPageObject
 */
public class JsonObject {

    public static final JsonObject SUCCESS = new JsonObject(null,0,"SUCCESS"){
        @Override
        public void setMessage(String message) {
            throw new UnsupportedOperationException("不允许调用SUCCESS单例对象的setMessage方法，请通过new JsonObject()的方式构造实例");
        }
        @Override
        public void setCode(int code) {
            throw new UnsupportedOperationException("不允许调用SUCCESS单例对象的setCode方法，请通过new JsonObject()的方式构造实例");
        }
        @Override
        public void setData(Object data) {
            throw new UnsupportedOperationException("不允许调用SUCCESS单例对象的setData方法，请通过new JsonObject()的方式构造实例");
        }
    };
    public static final JsonObject FAIL = new JsonObject(null,-1,"FAIL"){
        @Override
        public void setMessage(String message) {
            throw new UnsupportedOperationException("不允许调用FAIL单例对象的setMessage方法，请通过new JsonObject()的方式构造实例");
        }
        @Override
        public void setCode(int code) {
            throw new UnsupportedOperationException("不允许调用FAIL单例对象的setCode方法，请通过new JsonObject()的方式构造实例");
        }
        @Override
        public void setData(Object data) {
            throw new UnsupportedOperationException("不允许调用FAIL单例对象的setData方法，请通过new JsonObject()的方式构造实例");
        }
    };

    private Object data;
    private int code;
    private String message;

    public JsonObject(){}

    public JsonObject(Object data){
        this.data = data;
    }

    public JsonObject(Object data, int code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 此方法便于添加返回的键值对。如果调用了该方法设置键值对，表示JsonObject中的data属性对象类型即为Map类型。
     * 如果之前已经通过setData方法设置了其他非Map对象，则不能通过该方法设置新键值对。
     * @param name
     * @param value
     */
    public void addValue(String name,Object value){
        if(data == null){
            Map<String,Object> newData = new HashMap();
            newData.put(name,value);
            setData(newData);
        }else if(data instanceof Map){
            ((Map)data).put(name,value);
        }else if (this == SUCCESS || this == FAIL){
            throw new UnsupportedOperationException("不允许调用SUCCESS或FAIL单例对象的addValue方法，请通过new JsonObject()的方式构造实例");
        }else{
            throw new UnsupportedOperationException("当前JsonObject中的data属性不为null且不是Map对象，不能执行addValue方法添加键值对");
        }
    }
}
