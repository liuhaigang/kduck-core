package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import org.springframework.stereotype.Component;

@Component
public class UserConditionDefiner implements QueryConditionDefiner {
    @Override
    public String tableCode() {
        return "DEMO_USER";
    }

    @Override
    public DefaultCondition conditionDefine(BeanEntityDef entityDef) {
        BeanFieldDef enabled = entityDef.getFieldDef("enable");
        return new DefaultCondition(enabled, ConditionType.EQUALS,1);
    }
}
