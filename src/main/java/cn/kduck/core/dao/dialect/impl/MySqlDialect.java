package cn.kduck.core.dao.dialect.impl;

import cn.kduck.core.dao.dialect.DatabaseDialect;
import org.springframework.stereotype.Component;

@Component("MySqlDialect")
public class MySqlDialect implements DatabaseDialect {
    @Override
    public String productName() {
        return "MySQL";
    }

    @Override
    public String checkTable(String tableName) {
        return "SHOW TABLES LIKE '"+tableName+"'";
    }

    @Override
    public String pagingSql(String sql, int first, int maxRows) {
        if(first <= 0 && maxRows <= 0){
            return sql;
        }
        return sql + " LIMIT " + first + ","+ maxRows;
    }
}
