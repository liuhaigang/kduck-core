package cn.kduck.core.web.interceptor.operateinfo;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.service.ValueMap;

import java.util.Map;

/**
 * 审计操作对象，包含被操作的数据值及相关实体定义
 * @author LiuHG
 */
public class OperateObject {

    private final OperateType operateType;
    private final BeanEntityDef entityDef;
    private final ValueMap valueMap;

    public OperateObject(OperateType operateType,BeanEntityDef entityDef, Map map){
        this.operateType = operateType;
        this.entityDef = entityDef;
        this.valueMap = new ValueMap(map);
    }

    public OperateType getOperateType() {
        return operateType;
    }

    public BeanEntityDef getEntityDef() {
        return entityDef;
    }

    public ValueMap getValueMap() {
        return valueMap;
    }

    public enum OperateType {
        INSERT,UPDATE,DELETE,SELECT;
    }
}
