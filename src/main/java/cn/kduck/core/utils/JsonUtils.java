package cn.kduck.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils(){
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象"+object.getClass().getName()+"转换为Json字符串失败",e);
        }
    }

    public static void writeJson(Object object, OutputStream outputStream) {
        try {
            objectMapper.writeValue(outputStream,object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象"+object.getClass().getName()+"转换为Json失败",e);
        } catch (IOException e) {
            throw new RuntimeException("将json写入输出流时发生错误",e);
        }
    }

    public static <T> T jsonToObject(String json,Class<T> clazz) {
        try {
            return objectMapper.readValue(json,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json转换为对象"+clazz.getName()+"失败：" + json,e);
        }
    }

    public static <T> List<T> jsonToObjectList(String json, Class<T> clazz) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(List.class, clazz);
        try {
            return objectMapper.readValue(json,collectionType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json转换为对象"+clazz.getName()+"失败：" + json,e);
        }
    }
}
