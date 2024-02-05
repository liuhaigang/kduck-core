package cn.kduck.core.service;

import cn.kduck.core.service.ValueMap;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Function;

/**
 * LiuHG
 */
public class ParamMap {

    public static Param create(){
        return new Param();
    }

    /**
     *
     * @param name
     * @param value
     * @return
     * @deprecated 由{@link #createAndSet}代替
     */
    public static Param create(String name,Object value,Object... otherValues){
        return new Param(name,value,otherValues);
    }

    public static Param createAndSet(String name,Object value,Object... otherValues){
        return new Param(name,value,otherValues);
    }

    /**
     * 根据给定的Map挑选key为names的属性形成默认的Param对象，如果对应的name值为null，则忽略该属性。
     * @param map
     * @param names
     * @return
     */
    public static Param create(Map<String,Object> map,String... names){
        Param param = new Param();
        for (String name : names) {
            Object value = map.get(name);
            if(value != null){
                param.set(name,value);
            }
        }
        return param;
    }


    public static final class Param{

        private ValueMap paramMap = new ValueMap();

        public Param(){}

        public Param(String name,Object value,Object... otherValues){
            set(name,value,otherValues);
        }

        public Param set(String name,Object value,Object... otherValues){
            if(otherValues.length > 0){
                if(value == null){
                    throw new IllegalArgumentException("设置多个参数值时不允许null值");
                }
                Class<?> valueClass = value.getClass();
                //check class type
                for (Object otherValue : otherValues) {
                    if(otherValue == null){
                        throw new IllegalArgumentException("设置多个参数值时不允许null值");
                    }
                    if(otherValue.getClass() != valueClass){
                        throw new IllegalArgumentException("设置参数值错误，类型不一致：" + otherValue.getClass() + "!=" + valueClass);
                    }
                }

                Object[] elements = new Object[otherValues.length + 1];
                elements[0] = value;
                System.arraycopy(otherValues,0,elements,1,otherValues.length);
                paramMap.put(name,elements);
            }else{
                paramMap.put(name,value);
            }
            return this;
        }

        public Param setIfNotNull(String name,Object value){
            if(value != null){
                set(name,value);
            }
            return this;
        }

        public <T> Param set(String name,T value,Function<T,Boolean> conditionFunction){
            Boolean result = conditionFunction.apply(value);
            if(result != null && result.booleanValue()){
                set(name,value);
            }
            return this;
        }

        public Param pick(String... names) {
            Param resultParam = new Param();
            for (String n : names) {
                if(paramMap.containsKey(n)) {
                    Object v = paramMap.get(n);
                    resultParam.set(n,v);
                }
            }
            return resultParam;
        }

        public Map<String,Object> toMap(){
            return paramMap;
        }

        public <R extends ValueMap> R toMapBean(Function<Map,R> bean){
            return bean.apply(paramMap);
        }
    }
}
