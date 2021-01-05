package cn.kduck.core.utils;

import java.util.function.Function;

public final class ObjectUtils {

    private ObjectUtils(){}

    public static <T,R> boolean isNull(T obj, Function<T, R> objectField){
        if(obj == null) return true;
        return objectField.apply(obj) == null;
    }

    public static <T,R> R valueOrNull(T obj, Function<T, R> objectField){
        if(obj == null) return null;
        return objectField.apply(obj);
    }

    public static <T,R> R valueOrDefault(T obj, Function<T, R> objectField,R defaultValue){
        R r = valueOrNull(obj, objectField);
        return r == null ? defaultValue : r;
    }
}
