package cn.kduck.core.dao.query.formater.impl;

import cn.kduck.core.dao.query.formater.ValueFormatter;

import java.util.Map;

/**
 * LiuHG
 */
public class EnableValueFormatter implements ValueFormatter {

    @Override
    public Object format(Object value, Map<String,Object> valueMap) {
        if(value != null){
            String displayValue;
            if(value.equals("1")){
                displayValue = "启用";
            }else if(value.equals("0")){
                displayValue = "停用";
            }else{
                displayValue = value.toString();
            }
            return displayValue;
        }
        return null;
    }
}
