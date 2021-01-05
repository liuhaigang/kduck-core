package cn.kduck.core.dao.dialect.impl;

import cn.kduck.core.dao.dialect.DatabaseDialect;
import org.springframework.stereotype.Component;

@Component("Db2Dialect")
public class Db2Dialect implements DatabaseDialect {
    @Override
    public String productName() {
        return "BD2";
    }

    @Override
    public String checkTable(String tableName) {
        throw new RuntimeException("表存在检查失败，不支持的数据库类型：" + productName());
    }

    @Override
    public String pagingSql(String sql, int first, int maxRows) {
        if (first <= 0 && maxRows <= 0) {
            return sql;
        }
        return "select t.* from (select t.*,rownumber() over() as rn from (" + sql + ") t ) t" +
                " where t.rn between "+ (first + 1) +" and  " + (first + maxRows);
    }
}
