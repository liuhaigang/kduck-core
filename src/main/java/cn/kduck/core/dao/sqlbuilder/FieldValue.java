package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanFieldDef;

public class FieldValue {

    private final BeanFieldDef field;
    private final Object value;

    public FieldValue(BeanFieldDef field, Object value) {
        this.field = field;
        this.value = value;
    }

    public BeanFieldDef getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
