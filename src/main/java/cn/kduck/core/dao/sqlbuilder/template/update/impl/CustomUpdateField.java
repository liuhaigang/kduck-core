package cn.kduck.core.dao.sqlbuilder.template.update.impl;

import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.UpdateBuilder;
import cn.kduck.core.dao.sqlbuilder.template.update.UpdateFragmentTemplate;

import java.util.Map;

/**
 * 扩展更新字段模版，通过该模版对象可以定制更新语句SET后的字段参数。
 *
 * @author LiuHG
 * @see UpdateBuilder UpdateBuilder
 */
public class CustomUpdateField implements UpdateFragmentTemplate {
    private final String attrName;
    private final String paramName;

    public CustomUpdateField(String attrName){
        this.attrName = attrName;
        this.paramName = attrName;
    }

    public CustomUpdateField(String attrName, String paramName){
        this.attrName = attrName;
        this.paramName = paramName;
    }

    @Override
    public String getAttrName() {
        return attrName;
    }

    @Override
    public String buildFragment(BeanFieldDef fieldDef, Map<String, Object> paramMap) {
        String fieldName = fieldDef.getFieldName();
        return fieldName + " = " + "#{" + paramName + "}";
    }


}
