package cn.kduck.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * LiuHG
 */
public final class BeanMapUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private BeanMapUtils() {}

    public static Map<String,Object> toMap(Object obj){
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json,Map.class);
        } catch (Exception e) {
            throw new RuntimeException("toMap异常",e);
        }
    }

    public static <T> T toBean(Map<String,Object> valueMap,Class<T> cls){
        try {
            String json = objectMapper.writeValueAsString(valueMap);
            return objectMapper.readValue(json,cls);
        } catch (Exception e) {
            throw new RuntimeException("toBean常",e);
        }
    }

//    public static Map<String,Object> toMap(Object obj){
//
//        if(obj == null){
//            return null;
//        }
//
//        Map<String,Object> valueMap = new HashMap<>();
//        Class<?> cls = obj.getClass();
//
//        if(isMap(cls)){
//            Map mapObj = (Map) obj;
//            Iterator<String> keyIterator = mapObj.keySet().iterator();
//            while(keyIterator.hasNext()){
//                String keyName = keyIterator.next();
//                Object o = mapObj.get(keyName);
//                valueMap.put(keyName,getValueObject(o));
//            }
//            return valueMap;
//        }
//
//        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(cls);
//        for (PropertyDescriptor pd : propertyDescriptors) {
//            String propName = pd.getName();
//            if("class".equals(propName))continue;
//            Method readMethod = pd.getReadMethod();
//            Class<?> propertyType = pd.getPropertyType();
//
//            try {
//                Object objValue = readMethod.invoke(obj);
//                if(objValue == null){
//                    valueMap.put(propName,null);
//                }
//                if(isCollection(propertyType)){
//                    Collection listObj = (Collection)objValue;
//                    ArrayList arrayList = new ArrayList(listObj.size());
//                    for (Object o : listObj) {
//                        arrayList.add(toMap(o));
//                    }
//                    valueMap.put(propName,arrayList);
////                }else if(isMap(cls)){
////                    Map<String,Object> subMap = new HashMap<>();
////                    Map mapObj = (Map) obj;
////                    Iterator<String> keyIterator = mapObj.keySet().iterator();
////                    while(keyIterator.hasNext()){
////                        String keyName = keyIterator.next();
////                        Object o = mapObj.get(keyName);
////                        subMap.put(keyName,getValueObject(o));
////                    }
////                    valueMap.put(propName,subMap);
//                }else if(isArray(propertyType)){
//                    int length = Array.getLength(objValue);
//                    Object[] mapArray = (Object[]) Array.newInstance(Object.class, length);
//                    for (int i = 0; i < length; i++) {
//                        Object o = Array.get(objValue, i);
//                        mapArray[i] = getValueObject(o);
//                    }
//                    valueMap.put(propName,mapArray);
//                }else if(propertyType == Object.class){
//                    Map<String, Object> subMap = null;
//                    if(objValue != null){
//                        subMap = toMap(objValue);
//                    }
//                    valueMap.put(propName,subMap);
//                }
////                else if(isBasicType(propertyType)){
////                    valueMap.put(propName, obj);
////                }
//                else{
//                    valueMap.put(propName,getValueObject(objValue));
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Bean转换为Map时，执行readMethod方法时出现错误",e);
//            }
//        }
//        return valueMap;
//    }
//
//    private static Object getValueObject(Object o){
//        if(o == null) return null;
//        if(isBasicType(o.getClass())){
//            return o;
//        }else{
//            return toMap(o);
//        }
//    }
//
//    public static <T> T toBean(Map<String,Object> valueMap,Class<T> cls){
//        T o = BeanUtils.instantiateClass(cls);
//        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(cls);
//        for (PropertyDescriptor pd : propertyDescriptors) {
//            Object pv = valueMap.get(pd.getName());
//            if(pv != null){
//                Method writeMethod = pd.getWriteMethod();
//                Class<?> propertyType = pd.getPropertyType();
//                try {
//                    if(isBasicType(propertyType) || isMapOrCollection(propertyType)){
//                        writeMethod.invoke(o,ConversionUtils.convert(pv, propertyType));
//                    }else{
//                        writeMethod.invoke(o,toBean((Map)pv, propertyType));
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException("Map转换为Bean时，执行writeMethod方法时出现错误",e);
//                }
//            }
//        }
//        return o;
//    }
//
//    private static boolean isBasicType(Class objClass) {
//        if (Number.class.isAssignableFrom(objClass) ||
//                CharSequence.class.isAssignableFrom(objClass) ||
//                Date.class.isAssignableFrom(objClass) ||
//                Boolean.class.isAssignableFrom(objClass) || objClass.isPrimitive()) {
//            return true;
//        }
//        return false;
//    }
//
//    private static boolean isCollection(Class objClass) {
//        return Collection.class.isAssignableFrom(objClass);
//    }
//
//    private static boolean isMap(Class objClass) {
//        return Map.class.isAssignableFrom(objClass);
//    }
//
//    private static boolean isArray(Class objClass) {
//        return objClass.isArray();
//    }
//
//    private static boolean isMapOrCollection(Class objClass) {
//
//        if (Collection.class.isAssignableFrom(objClass) ||
//                Map.class.isAssignableFrom(objClass) ||
//                objClass.isArray()) {
//            return true;
//        }
//        return false;
//    }
}
