package cn.kduck.core.dao.sqllog.impl;

import cn.kduck.core.dao.sqllog.ShowSqlLogger;

import java.util.List;

public class EmptyShowSqlLogger implements ShowSqlLogger {

    @Override
    public void sqlLog(String sql, List<Object> paramValueList, String generateBy, boolean violate) {

    }

    @Override
    public void timeSqlLog(long milliseconds, String sql, List<Object> paramValueList, String generateBy, boolean violate) {

    }

    @Override
    public void errorSqlLog(String sql, List<Object> paramValueList, Exception exception, String generateBy) {

    }
}
