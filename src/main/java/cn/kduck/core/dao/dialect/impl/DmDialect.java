package cn.kduck.core.dao.dialect.impl;

import cn.kduck.core.dao.dialect.DatabaseDialect;
import org.springframework.stereotype.Component;

@Component("DmDialect")
public class DmDialect implements DatabaseDialect {
    @Override
    public String productName() {
        return "DM DBMS";
    }

    @Override
    public String checkTable(String tableName) {
        return "select 1 from user_tables where table_name='" + tableName + "'";
    }

    @Override
    public String pagingSql(String sql, int first, int maxRows) {
        String sql_ = " select * from (select t.*, rownum rnum from (" + sql + ") t ";
        if (maxRows >= 0) {
            sql_ += " where rownum <= " + (first + maxRows);
        }
        sql_ +=  ") ";
        if (first >= 0) {
            sql_ += " where rnum >= " +  (first + 1);
        }
        return sql_;
    }
}
