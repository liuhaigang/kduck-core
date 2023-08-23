package cn.kduck.core.service;

import cn.kduck.core.service.ParamMap.Param;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.query.formater.ValueFormatter;
import cn.kduck.core.utils.ConversionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.utils.ValueMapUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 统一的业务对象，负责在业务层传递数据。本对象继承HashMap，因此含有Map的所有特性。为了便于使用，该类中包含了大量返回具体类型值的方法便于使用。
 * 同时可调用{@link #formatValue(String, ValueFormatter)}方法对数据值进行格式化。
 * @author LiuHG
 */
public class ValueMap extends HashMap<String,Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final ValueMap EMPTY_VALUE_MAP = new ValueMap(Collections.emptyMap());

    public ValueMap(){}

    /**
     * 根据一个Map构造
     * @param map
     */
    public ValueMap(Map<String,Object> map){
        super(map);
    }

    public <T> T convert(Class<T> targetClass){
        T targetObject = BeanUtils.instantiateClass(targetClass);
        if(targetObject instanceof Map){
            ((Map)targetObject).putAll(this);
        }else{
            Iterator<String> keySet = super.keySet().iterator();
            while(keySet.hasNext()){
                String keyName = keySet.next();
                Object value = super.get(keyName);
                if(value != null) {
                    PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(targetClass, keyName);
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    Class<?> propertyType = propertyDescriptor.getPropertyType();
                    try {
                        writeMethod.invoke(targetObject, ConversionUtils.convert(value,propertyType));
                    } catch (Exception e) {
                        throw new RuntimeException("根据" + targetClass.getName() +"转换错误：name=" + keyName +",value=" + value,e);
                    }
                }
            }
        }
        return targetObject;
    }

    public <R extends ValueMap> R convert(Function<Map,R> bean){
        R r = bean.apply(this);
        return r;
    }

    /**
     * 方法功能效果同put方法，此方法仅与get方法保持一致风格。
     * @param name 属性名
     * @param value 属性值
     */
    public void setValue(String name,Object value){
        super.put(name,value);
    }

    public void setValue(String name, Object value, ValueFormatter formatter){
        super.put(name,formatter.format(value,this));
    }

    /**
     * 获取属性值并转换成字符串类型。如果值不存在，返回null
     * @param name 属性名
     * @return 字符类型的值
     */
    public String getValueAsString(String name) {
        return ValueMapUtils.getValueAsString(this,name);
    }

    /**
     * 获取属性值并转换成整型包装类型。如果值不存在，返回null
     * @param name 属性名
     * @return 整型的包装类型值
     * @see #getValueAsInt(String)
     */
    public Integer getValueAsInteger(String name) {
        return ValueMapUtils.getValueAsInteger(this,name);
    }

    /**
     * 获取属性值并转换成长整型包装类型。如果值不存在，返回null
     * @param name 属性名
     * @return 长整型的包装类型值
     * @see #getValueAsInt(String)
     */
    public Long getValueAsLong(String name) {
        return ValueMapUtils.getValueAsLong(this,name);
    }

    /**
     * 获取属性值并转换成整型的基本配型类型。如果值不存在，返回0
     * @param name 属性名
     * @return 整型的基本配型的值
     * @see #getValueAsInteger(String)
     */
    public int getValueAsInt(String name) {
        return ValueMapUtils.getValueAsInt(this,name);
    }

    /**
     * 获取属性值并转换成日期类型。如果值不存在，返回null
     * @param name 属性名
     * @return 日期类型值
     */
    public Date getValueAsDate(String name) {
        return ValueMapUtils.getValueAsDate(this,name);
    }

    /**
     * 获取属性值并转换成双精度浮点类型。如果值不存在，返回null
     * @param name 属性名
     * @return 双精度浮点类型值
     */
    public Double getValueAsDouble(String name) {
        return ValueMapUtils.getValueAsDouble(this,name);
    }

    /**
     * 获取属性值并转换成布尔的基本类型。如果值不存在，返回false
     * @param name 属性名
     * @return 布尔的基本配型值
     */
    public boolean getValueAsBool(String name) {
        return ValueMapUtils.getValueAsBool(this,name);
    }

    /**
     * 获取属性值并转换成浮点类型。如果值不存在，返回null
     * @param name 属性名
     * @return 浮点类型值
     */
    public Float getValueAsFloat(String name) {
        return ValueMapUtils.getValueAsFloat(this,name);
    }

    /**
     * 获取属性值并转换成布尔包装类型，如果值不存在，返回null
     * @param name 属性名
     * @return 布尔包装类型值
     */
    public Boolean getValueAsBoolean(String name) {
        return ValueMapUtils.getValueAsBoolean(this,name);
    }

    /**
     * 获取属性值并转换成List类型。如果值不存在，返回空的集合。
     * @param name 属性名
     * @return List类型值
     */
    public List getValueAsList(String name) {
        return ValueMapUtils.getValueAsList(this,name);
    }

    public <T> T[] getValueAsArray(String name,Class<T> clazz) {
        return ValueMapUtils.getValueAsArray(this,name,clazz);
    }

    /**
     * 获取属性值并转换成ValueMapList类型。如果值不存在，返回空的集合。<br/>
     * 要求List中的元素均是Map或其构造的子类对象，否则会抛出异常
     * @param name 属性名
     * @return ValueMapList
     */
    public ValueMapList getValueAsValueMapList(String name) {
        List valueList = getValueAsList(name);
        ValueMapList valueMapList = new ValueMapList(valueList.size());
        for (Object value : valueList) {
            if(value instanceof Map){
                valueMapList.add(new ValueMap((Map)value));
            }else{
                throw new IllegalArgumentException("转换ValueMapList失败，集合中的属性必须为Map或其构造的子类对象:" + value.getClass());
            }
        }
        return valueMapList;
    }

    /**
     * 获取属性值并转换成Map类型。如果值不存在，返回空的Map。如果值为Map类型则直接返回，否则以name为key，以值为value返回一个新Map
     * @param name 属性名
     * @return Map类型值
     */
    public Map getValueAsMap(String name) {
        return ValueMapUtils.getValueAsMap(this,name);
    }

    /**
     * 获取属性值并转换成ValueMap类型。如果值不存在，返回空的Map。如果值为Map类型则直接返回，否则以name为key，以值为value返回一个新Map
     * @param name 属性名
     * @return ValueMap类型值
     */
    public ValueMap getValueAsValueMap(String name) {
        return new ValueMap(getValueAsMap(name));
    }

    /**
     * 将指定值转换为指定Class的类实例，要求name对应的值是一个合法的json结构数据。
     * @param name
     * @param clazz
     * @param <T>
     * @return 转换后的实例对象
     */
    public <T> T getValueAsBean(String name,Class<T> clazz) {
        String json = getValueAsString(name);
        try {
            return objectMapper.readValue(json,clazz);
        } catch (IOException e) {
            throw new RuntimeException("json转换为"+clazz.getName()+"时发生错误：" + json ,e);
        }
    }

    public boolean hasValue(String name){
        return super.get(name) != null ? true : false;
    }

    /**
     * 用于数据保存前进行值格式化，调用该方法的同时值即被转换。如果期望查询后的格式化，使用
     * {@link QuerySupport#addValueFormatter(String, ValueFormatter) QuerySupport#addValueFormatter(String, ValueFormatter) }
     * 方法设置。
     * @param name 属性名
     * @param formatter 格式化接口
     * @see QuerySupport#addValueFormatter(String, ValueFormatter)
     */
    public void formatValue(String name, ValueFormatter formatter){
        if(super.containsKey(name)){
            Object value = super.get(name);
            super.put(name,formatter.format(value,this));
        }
    }

    /**
     * 根据指定的属性名，构造一个新的Map对象。
     * @param paramName 属性名
     * @param paramNames 属性名
     * @return 根据指定属性名构造的一个新的Map对象
     */
    public Map<String,Object> createMap(String paramName,String... paramNames){
        Param param = ParamMap.create(paramName, get(paramName));
        for (String pName : paramNames) {
            param.set(pName,get(pName));
        }
        return param.toMap();
    }

    /**
     * 将指定的属性从当前ValueMap中删除
     * @param names
     */
    public void removeValue(String... names){
        for (String name : names) {
            super.remove(name);
        }
    }

    /**
     * 将指定的属性名设置值为null，如果指定的name在当前的ValueMap中不存在，则忽略
     * @param names
     */
    public void setNullValue(String... names){
        for (String name : names) {
            if(containsKey(name)){
                setValue(name,null);
            }
        }
    }
}
