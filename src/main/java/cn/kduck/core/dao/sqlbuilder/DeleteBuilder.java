package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanEntityDef;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 删除语句构造器
 * @author LiuHG
 */
public class DeleteBuilder extends UpdateBuilder{


    public DeleteBuilder(BeanEntityDef entityDef, Map<String, Object> paramMap) {
        super(entityDef, paramMap);
    }


    protected String toSql(){
        if(conditionBuilder == null){
            throw new RuntimeException("【违规范】删除必须包含删除条件");
        }

        String conditionSql = conditionBuilder.toCondition(paramMap,true);

        if(StringUtils.isEmpty(conditionSql)){
            List<String> conditionAttrNames = conditionBuilder.getConditionAttrNames();
            throw new RuntimeException("【违规范】删除必须包含删除条件，虽然设置了可用删除条件：" + conditionAttrNames + "，但无一命中");
        }

        StringBuilder sqlBuidler = new StringBuilder("DELETE FROM ");
        sqlBuidler.append(entityDef.getTableName())
                .append(conditionSql);

        return sqlBuidler.toString();
    }
}
