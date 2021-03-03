package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.service.ParamMap;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;


/**
 * @author LiuHG
 */
//@SpringBootTest
@TestMethodOrder(MethodName.class)
public class ConditionBuilderTest {


    @Test
    public void build000() {
        Map<String, Object> paramMap = ParamMap.create("param1", "value1").set("param2", "value2").set("param3", "value3").toMap();

        ConditionBuilder conditionBuilder = newBuilder();
        conditionBuilder.and("FIELD1", ConditionType.EQUALS,"param1")
                .and("FIELD2", ConditionType.CONTAINS,"param2");
        String sql = conditionBuilder.toCondition(paramMap, true);
        Assertions.assertEquals(" WHERE FIELD1 = #{param1} AND FIELD2 LIKE #{param2}",sql);

        sql = conditionBuilder.toCondition(paramMap, false);
        Assertions.assertEquals(" AND FIELD1 = #{param1} AND FIELD2 LIKE #{param2}",sql);

        conditionBuilder = newBuilder();
        paramMap.remove("param1");
        conditionBuilder.and("FIELD1", ConditionType.EQUALS,"param1")
                .and("FIELD2", ConditionType.CONTAINS,"param2");
        sql = conditionBuilder.toCondition(paramMap, true);
        Assertions.assertEquals(" WHERE FIELD2 LIKE #{param2}",sql);

        sql = conditionBuilder.toCondition(paramMap, false);
        Assertions.assertEquals(" AND FIELD2 LIKE #{param2}",sql);
    }

    @Test
    public void build001() {
        Map<String, Object> paramMap = ParamMap.create("param1", "value1").set("param2", "value2").set("param3", "value3").toMap();

        ConditionBuilder conditionBuilder = newBuilder();
        conditionBuilder.or("FIELD1", ConditionType.EQUALS,"param1")
                .or("FIELD2", ConditionType.GREATER,"param2")
                .and("FIELD3", ConditionType.BEGIN_WITH,"param3");
        String sql = conditionBuilder.toCondition(paramMap, true);
        Assertions.assertEquals(" WHERE FIELD1 = #{param1} OR FIELD2 > #{param2} AND FIELD3 LIKE #{param3}",sql);

        sql = conditionBuilder.toCondition(paramMap, false);
        Assertions.assertEquals(" OR FIELD1 = #{param1} OR FIELD2 > #{param2} AND FIELD3 LIKE #{param3}",sql);

    }


    private ConditionBuilder newBuilder(){
        return new ConditionBuilder(){
            @Override
            protected boolean checkRequired(String attrName) {
                return true;
            }
        };
    }
}