package cn.kduck.core.dao.dialect.impl;

import cn.kduck.core.dao.dialect.DatabaseDialect;
import org.springframework.stereotype.Component;

@Component("H2Dialect")
public class H2Dialect implements DatabaseDialect {
    @Override
    public String productName() {
        return "H2";
    }

    @Override
    public String checkTable(String tableName) {
        return "show tables like '"+tableName+"'";
    }

    @Override
    public String pagingSql(String sql, int first, int maxRows) {
        if(first <= 0 && maxRows <= 0){
            return sql;
        }
        return sql + " limit " + first + ","+ maxRows;
    }
}
