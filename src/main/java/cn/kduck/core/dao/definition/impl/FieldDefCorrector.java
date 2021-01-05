package cn.kduck.core.dao.definition.impl;

import cn.kduck.core.dao.definition.BeanFieldDef;

public interface FieldDefCorrector {

    void correct(String tableName,BeanFieldDef fieldDef);
}
