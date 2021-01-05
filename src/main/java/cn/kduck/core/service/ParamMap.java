package cn.kduck.core.service;

import cn.kduck.core.service.ValueMap;

import java.util.Map;
import java.util.function.Function;

/**
 * LiuHG
 */
public class ParamMap {

    public static Param create(){
        return new Param();
    }

    public static Param create(String name,Object value){
        return new Param(name,value);
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

        public Param(String name,Object value){
            paramMap.put(name,value);
        }

        public Param set(String name,Object value){
            paramMap.put(name,value);
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
