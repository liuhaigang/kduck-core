package cn.kduck.core.dao.sqllog;

import java.util.List;

public interface ShowSqlLogger {

    default void sqlLog(String sql, List<Object> paramValueList) {
        sqlLog(sql,paramValueList,null,false);
    }

    default void sqlLog(String sql, List<Object> paramValueList,String generateBy) {
        sqlLog(sql,paramValueList,generateBy,false);
    }

    void sqlLog(String sql, List<Object> paramValueList,String generateBy,boolean violate);


    default void timeSqlLog(long milliseconds,String sql, List<Object> paramValueList){
        timeSqlLog(milliseconds,sql,paramValueList,null,false);
    }

    default void timeSqlLog(long milliseconds,String sql, List<Object> paramValueList,String generateBy){
        timeSqlLog(milliseconds,sql,paramValueList,generateBy,false);
    }

    void timeSqlLog(long milliseconds,String sql, List<Object> paramValueList,String generateBy,boolean violate);


    default void errorSqlLog(String sql, List<Object> paramValueList,Exception exception){
        errorSqlLog(sql,paramValueList,exception,null);
    }

    void errorSqlLog(String sql, List<Object> paramValueList,Exception exception,String generateBy);
}
