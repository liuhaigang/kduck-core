package cn.kduck.core.utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * LiuHG
 */
public final class ValueMapUtils {

    private ValueMapUtils(){}

    public static String getValueAsString(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,String.class);
    }

    public static Integer getValueAsInteger(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Integer.class);
    }

    public static Long getValueAsLong(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Long.class);
    }

    public static int getValueAsInt(Map<String,Object> valueMap,String name) {
        Integer v = getValueAsInteger(valueMap,name);
        return v == null ? 0 : v.intValue();
    }

    public static Date getValueAsDate(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Date.class);
    }

    public static Double getValueAsDouble(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Double.class);
    }

    public static boolean getValueAsBool(Map<String,Object> valueMap,String name) {
        Boolean v = getValueAsBoolean(valueMap,name);
        return v == null ? false : v.booleanValue();
    }

    public static Float getValueAsFloat(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Float.class);
    }

    public static Boolean getValueAsBoolean(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        return getValue(v,Boolean.class,false);
    }

    public static List getValueAsList(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        if(v == null){
            return Collections.emptyList();
        }
        if(v instanceof List){
            return (List)v;
        }

        ArrayList arrayList = new ArrayList();
        arrayList.add(v);
        return arrayList;
    }

    public static <T> T[] getValueAsArray(Map<String,Object> valueMap,String name,Class<T> clazz){
        Object v = valueMap.get(name);
        if(v != null){
            if(v.getClass().isArray()) {
                return (T[])v;
            }if(v instanceof Collection) {
                Collection collection = (Collection) v;
                T[] vArray = (T[]) Array.newInstance(clazz,collection.size());
                int i = 0;
                for (Object o : collection) {
                    vArray[i++] = ConversionUtils.convert(o,clazz);
                }
                return vArray;
            }else{
               T[] vArray = (T[]) Array.newInstance(clazz,1);
               vArray[0] = ConversionUtils.convert(v,clazz);
               return vArray;
            }
        }

        return (T[]) Array.newInstance(clazz,0);
    }

    public static Map getValueAsMap(Map<String,Object> valueMap,String name) {
        Object v = valueMap.get(name);
        if(v == null){
            return Collections.emptyMap();
        }
        if(v instanceof Map){
            return (Map)v;
        }
        HashMap hashMap = new HashMap();
        hashMap.put(name,v);
        return hashMap;
    }

    public static <T,R> R convert(Function<T,R> bean, T valueMap){
        if(valueMap == null){
            return null;
        }
        return bean.apply(valueMap);
    }

    private static <T> T getValue(Object v,Class<T> cls){
        return getValue(v,cls,null);
    }

    private static <T> T getValue(Object v,Class<T> cls,Object defaultValue){
        if(v == null){
            v =  defaultValue;
        }

        if(v == null){
            return null;
        }
        return ConversionUtils.convert(v,cls);
    }

}
