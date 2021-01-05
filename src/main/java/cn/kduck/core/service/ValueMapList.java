package cn.kduck.core.service;

import cn.kduck.core.utils.ConversionUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author LiuHG
 */
public class ValueMapList extends ArrayList<ValueMap> {

    public static final ValueMapList EMPTY_LIST = new ValueMapList();

    public ValueMapList(){}

    public ValueMapList(int initialCapacity){
        super(initialCapacity);
    }

    public ValueMapList(List<Map<String, Object>> valueList){
        super(valueList.size());
        for (Map<String, Object> map : valueList) {
            super.add(new ValueMap(map));
        }
    }

    /**
     * 提取ValueMapList中的一个属性作为集合返回，由于List中Map的不一定指定的属性都存在，所以返回的List长度可能会比ValueMapList的
     * 长度要小。需要注意的是，本方法不会忽略值为null的属性，可能会返回类似["vaule1",null,"value3"]的集合，如需要过滤掉null值，请参考{@link #getValueList(String, boolean)}
     * @param name 指定返回的属性名
     * @return 指定属性的值集合
     * @see #getValueList(String, boolean)
     */
    public List<Object> getValueList(String name){
        return getValueList(name,false);
    }

    public <T> List<T> getValueList(String name,Class<T> type){
        return getValueList(name,type,false);
    }

    /**
     * 提取ValueMapList中的一个属性作为数组返回，由于数组中Map的不一定指定的属性都存在，所以返回的数组长度可能会比ValueMapList的
     * 长度要小。需要注意的是，本方法不会忽略值为null的属性，可能会返回类似["vaule1",null,"value3"]的数组，如需要过滤掉null值，请参考{@link #getValueArray(String, Class,boolean)}
     * @param name 指定返回的属性名
     * @return 指定属性的值集合
     * @see #getValueArray(String, Class,boolean)
     */
    public <T> T[] getValueArray(String name,Class<T> type){
        return getValueArray(name,type,false);
    }

    public <T> T[] getValueArray(String name,Class<T> type,boolean ignoreNull){
        List<T> valueList = getValueList(name, type, ignoreNull);
        return (T[]) valueList.toArray();
    }

    public <T extends ValueMap> List<T> convertList(Class<T> targetClass){
        List<T> reslutList = new ArrayList<>(super.size());
        for (ValueMap valueMap : this) {
            T targetObject = BeanUtils.instantiateClass(targetClass);
            targetObject.putAll(valueMap);
            reslutList.add(targetObject);
        }
        return reslutList;
    }

    public <T extends ValueMap> List<T> convertList(Function<Map,T> bean){
        List<T> reslutList = new ArrayList<>(super.size());
        for (ValueMap valueMap : this) {
            T targetObject = bean.apply(valueMap);
            targetObject.putAll(valueMap);
            reslutList.add(targetObject);
        }
        return reslutList;
    }

    /**
     * 提取ValueMapList中的一个属性作为集合返回，由于List中Map的不一定指定的属性都存在，所以返回的List长度可能会比ValueMapList的
     * 长度要小。
     * @param name 指定返回的属性名
     * @param ignoreNull 是否忽略值为null的属性
     * @return 指定属性的值集合
     */
    public List<Object> getValueList(String name,boolean ignoreNull){
        List<Object> valueList = new ArrayList(super.size());
        for (Map<String,Object> valueMap : this) {
            if(valueMap.containsKey(name)){
                Object v = valueMap.get(name);
                if(ignoreNull && v == null) continue;
                valueList.add(v);
            }
        }
        return valueList;
    }

    public <T> List<T> getValueList(String name,Class<T> type,boolean ignoreNull){
        List<T> valueList = new ArrayList(super.size());
        for (Map<String,Object> valueMap : this) {
            if(valueMap.containsKey(name)){
                Object v = valueMap.get(name);
                if(ignoreNull && v == null) continue;
                valueList.add(ConversionUtils.convert(v,type));
            }
        }
        return valueList;
    }

}
