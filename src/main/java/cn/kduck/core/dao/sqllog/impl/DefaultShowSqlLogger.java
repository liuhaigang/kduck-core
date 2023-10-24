package cn.kduck.core.dao.sqllog.impl;

import cn.kduck.core.KduckProperties.ShowSqlMode;
import cn.kduck.core.KduckProperties.ShowSqlProperties;
import cn.kduck.core.dao.datasource.DataSourceSwitch;
import cn.kduck.core.dao.sqllog.ShowSqlLogger;
import cn.kduck.core.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.jdbc.core.SqlParameterValue;

import java.io.PrintStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DefaultShowSqlLogger implements ShowSqlLogger {

    private final PrintStream writer;
    private final ShowSqlProperties showSqlProperties;
    private ObjectMapper jsonMapper = new ObjectMapper();

    //慢sql的输出阈值，500毫秒
    public static final int SLOW_SQL_MILLISECOND_THRESHOLD = 500;

    public DefaultShowSqlLogger(PrintStream writer, ShowSqlProperties showSqlProperties){
        this.writer = writer;
        this.showSqlProperties = showSqlProperties;
    }

    @Override
    public void sqlLog(String sql, List<Object> paramValueList,String generateBy, boolean violate) {
        printSql(-1,sql,paramValueList,generateBy,violate);
    }

    @Override
    public void timeSqlLog(long milliseconds, String sql, List<Object> paramValueList,String generateBy, boolean violate) {
        printSql(milliseconds,sql,paramValueList,generateBy,violate);
    }

    @Override
    public void errorSqlLog(String sql, List<Object> paramValueList, Exception exception, String generateBy) {
        printSql(-1,sql,paramValueList,generateBy,false);
    }

    protected void printSql(long time, String sql, List<Object> paramList,String generateBy,boolean violate){
        if(showSqlProperties != null
                && showSqlProperties.getMode() == ShowSqlMode.JUST_SLOW_SQL
                && time < SLOW_SQL_MILLISECOND_THRESHOLD){
            //如果开启慢sql日志，则如果未达到阈值则直接返回。
            return;
        }
        List<Object> printParam = new ArrayList<>(paramList.size());
        String paramJson;
        try {
            for (Object rowParam : paramList) {
                if (rowParam.getClass().isArray()) {
                    Object[] paramItems = (Object[]) rowParam;
                    Object[] tempItems = new Object[paramItems.length];
                    for (int i1 = 0; i1 < paramItems.length; i1++) {
                        Object paramItem = paramItems[i1];
                        tempItems[i1] = unwrapParamValue(paramItem);
                    }
                    printParam.add(tempItems);
                } else {
                    printParam.add(unwrapParamValue(rowParam));
                }
            }
            paramJson =  jsonMapper.writeValueAsString(printParam);
        } catch (JsonProcessingException e) {
            paramJson = "【参数值转换JSON错误】";
        }

        String spendTime = "";
        if(time >= 0 ){
            AnsiElement color = time >= SLOW_SQL_MILLISECOND_THRESHOLD ? AnsiColor.RED:AnsiColor.YELLOW;
            spendTime = AnsiOutput.toString(color,"(" + time + "ms)");
        }

        String violateText = "";
        if(violate){
            violateText =  AnsiOutput.toString(AnsiColor.RED,"【违反规范】");
        }

        String queryText = generateBy != null ? AnsiOutput.toString("; ",AnsiColor.YELLOW,"QUERY:",AnsiColor.DEFAULT,generateBy) : "";

        String dsKey = "";
        if(DataSourceSwitch.isEnabled()){
            dsKey = "[" + DataSourceSwitch.get() + "]";
        }

        String printSql = AnsiOutput.toString(
                AnsiStyle.BOLD,
                violateText,
                spendTime,
                AnsiStyle.BOLD,
                AnsiColor.YELLOW,
                dsKey,
                "SQL:",
                AnsiColor.BLUE,
                sql+"; ",
                AnsiColor.YELLOW,
                "PARAMS:",
                AnsiColor.DEFAULT,
                paramJson,
                queryText,
                AnsiStyle.NORMAL);
        writer.println(printSql);
    }

    private Object unwrapParamValue(Object paramItem) {
        if(paramItem instanceof SqlParameterValue){
            SqlParameterValue pv = (SqlParameterValue)paramItem;
            Object value = pv.getValue();
            if(value != null && (pv.getSqlType() == Types.LONGVARCHAR || pv.getSqlType() == Types.LONGNVARCHAR || pv.getSqlType() == Types.LONGVARBINARY)){
                return "<LOB>";
            }else{
                return value;
            }
        }else{
            return paramItem;
        }
    }
}
