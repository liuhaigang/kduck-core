package cn.kduck.core.dao.sqlbuilder.template.update.impl;

import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.UpdateBuilder;
import cn.kduck.core.dao.sqlbuilder.template.update.UpdateFragmentTemplate;

import java.util.Map;

/**
 * 字段自增更新模版，用于字段根据条件自增的情况，拼写类似FIELD_NAME=FIELD_NAME+1的SQL片段
 * @author LiuHG
 * @see UpdateBuilder UpdateBuilder
 */
public class FieldIncrease implements UpdateFragmentTemplate {

    private final String attrName;
    private final int step;

    public FieldIncrease(String attrName,int step){
        this.attrName = attrName;
        this.step = step;
    }

    public FieldIncrease(String attrName){
        this.attrName = attrName;
        this.step = 1;
    }

    @Override
    public String buildFragment(BeanFieldDef fieldDef, Map<String, Object> paramMap) {
        String fieldName = fieldDef.getFieldName();
        return fieldName + " = " + fieldName + "+" + step;
    }

    @Override
    public String getAttrName() {
        return attrName;
    }
}
