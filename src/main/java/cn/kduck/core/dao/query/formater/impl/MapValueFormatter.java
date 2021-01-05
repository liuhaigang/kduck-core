package cn.kduck.core.dao.query.formater.impl;

import cn.kduck.core.dao.query.formater.ValueFormatter;

import java.util.Map;

/**
 * LiuHG
 */
public class MapValueFormatter implements ValueFormatter {

    private final Map<String, String> mappingMap;

    public MapValueFormatter(Map<String,String> mappingMap){
        this.mappingMap = mappingMap;
    }

    @Override
    public Object format(Object value, Map<String,Object> valueMap) {
        if(value != null){
            return mappingMap.get(String.valueOf(value));
        }
        return null;
    }
}
