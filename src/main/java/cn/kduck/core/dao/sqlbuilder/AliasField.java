package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanFieldDef;

/**
 * 含别名的字段对象
 * @author LiuHG
 */
public class AliasField {

    private String alias;
    private final BeanFieldDef fieldDef;

    public AliasField(BeanFieldDef fieldDef) {
        this.alias = null;
        this.fieldDef = fieldDef;
    }

    public AliasField(String alias, BeanFieldDef fieldDef) {
        this.alias = alias;
        this.fieldDef = fieldDef;
    }

    public String getAlias() {
        return alias;
    }

    public BeanFieldDef getFieldDef() {
        return fieldDef;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
