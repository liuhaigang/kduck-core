package cn.kduck.core.dao.sqlbuilder.utils;

import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;

import java.util.Collection;
import java.util.Map;

public final class ConditionTypeUtils {

    private ConditionTypeUtils(){}

    public static ConditionType equalOrIn(Map paramMap, String attrName){
        Object value = paramMap.get(attrName);
        ConditionType conditionType = ConditionType.EQUALS;
        if(value != null && (value.getClass().isArray() || value instanceof Collection)){
            conditionType = ConditionType.IN;
        }
        return conditionType;
    }

}
