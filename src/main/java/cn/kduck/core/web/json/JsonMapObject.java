package cn.kduck.core.web.json;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LiuHG
 */
public class JsonMapObject extends JsonObject{

    public JsonMapObject() {
        super(new HashMap());
    }

    public JsonMapObject(int code, String message) {
        super(new HashMap(), code, message);
    }

    public void put(String name,Object value){
        Object data = super.getData();
        ((Map)data).put(name,value);
    }

    public Object get(String name,Object value){
        Object data = super.getData();
        return ((Map)data).get(name);
    }
}
