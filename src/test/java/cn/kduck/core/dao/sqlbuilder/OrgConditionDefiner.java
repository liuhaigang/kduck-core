package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import org.springframework.stereotype.Component;

@Component
public class OrgConditionDefiner implements QueryConditionDefiner {
    @Override
    public String tableCode() {
        return "DEMO_ORG";
    }

    @Override
    public DefaultCondition conditionDefine(BeanEntityDef entityDef) {
        BeanFieldDef enabled = entityDef.getFieldDef("orgCode");
        return new DefaultCondition(enabled, ConditionType.EQUALS,"xxx","XXXX");
    }
}
