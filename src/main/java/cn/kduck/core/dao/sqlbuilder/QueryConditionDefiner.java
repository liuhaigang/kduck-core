package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * 默认查询条件定义器
 * @author LiuHG
 */
public interface QueryConditionDefiner {

    String tableCode();

    DefaultCondition conditionDefine(BeanEntityDef entityDef);

    class DefaultCondition {
        private final BeanFieldDef fieldDef;
        private final ConditionType type;
        private final String paramName;
        private final Object defaultValue;
        private final boolean allowOverride;

        public DefaultCondition(BeanFieldDef fieldDef, ConditionType type, Object defaultValue) {
            this(fieldDef,type,null,defaultValue,false);
        }

        public DefaultCondition(BeanFieldDef fieldDef, ConditionType type, String paramName, Object defaultValue) {
            this(fieldDef,type,paramName,defaultValue,true);
        }

        public DefaultCondition(BeanFieldDef fieldDef, ConditionType type, String paramName,Object defaultValue, boolean allowOverride) {
            Assert.notNull(fieldDef,"查询条件定义器配置错误，字段对象不能为null");
            this.fieldDef = fieldDef;
            this.type = type;
            this.defaultValue = defaultValue;
            this.allowOverride = allowOverride;
            if(paramName == null){
                if(allowOverride){
                    throw new IllegalArgumentException("查询条件定义器配置错误，当allowOverride=true时，必须指定paramName");
                }
                paramName = UUID.randomUUID().toString().replaceAll("-","");
            }
            this.paramName = paramName;
        }

        public BeanFieldDef getFieldDef() {
            return fieldDef;
        }

        public ConditionType getType() {
            return type;
        }

        public String getParamName() {
            return paramName;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public boolean isAllowOverride() {
            return allowOverride;
        }
    }
}
