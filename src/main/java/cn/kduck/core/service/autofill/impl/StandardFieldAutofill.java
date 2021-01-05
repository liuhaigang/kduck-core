package cn.kduck.core.service.autofill.impl;

import cn.kduck.core.service.ValueBean;
import cn.kduck.core.service.autofill.AutofillValue;

import java.util.Date;
import java.util.Map;

/**
 * 标准字段填充
 * @author LiuHG
 */
public class StandardFieldAutofill implements AutofillValue {


    @Override
    public void autofill(FillType type, ValueBean valueBean) {
        Map<String, Object> valueMap = valueBean.getValueMap();

        if(type == FillType.ADD){
            fillValue(valueMap,StandardField.CREATE_TIME,StandardField.CREATE_USER_ID,StandardField.CREATE_USER_NAME);
        }else if(type == FillType.UPDATE){
            fillValue(valueMap, StandardField.MODIFY_TIME);
        }
    }

    private void fillValue(Map valueMap, StandardField... allFields) {
        for (StandardField field : allFields) {
            String attrName = field.getAttrName();

            Object value = getValue(field);
            if(value != null){
                valueMap.put(attrName,value);
            }
        }
    }

    private Object getValue(StandardField field){
        Object value = null;
        Date now = new Date();
        switch (field){
            case CREATE_TIME:value = now;break;
            case MODIFY_TIME:value = now;break;
            case CREATE_USER_NAME:/* 获取用户名 */break;
            case CREATE_USER_ID:/* 获取用户ID */break;
        }
        return value;
    }

    public enum StandardField {
        CREATE_TIME("createTime"),MODIFY_TIME("modifyTime"),CREATE_USER_NAME("createUserName"),CREATE_USER_ID("createUserId");

        private final String stdAttrName;

        StandardField(String stdAttrName){
            this.stdAttrName = stdAttrName;
        }

        public String getAttrName(){
            return stdAttrName;
        }
    }
}
