package cn.kduck.core.dao.utils;

import java.io.Reader;
import java.sql.Types;
import java.util.Date;

/**
 * LiuHG
 */
public final class TypeUtils {

    private TypeUtils(){}

    public static int jdbcType(Class javaType){
        if(javaType == String.class){
            return Types.VARCHAR;
        } else if(javaType == Integer.class){
            return Types.INTEGER;
        } else if(javaType == Date.class){
            return Types.TIMESTAMP;
        } else if(javaType == Long.class){
            return Types.BIGINT;
        } else if (javaType == Double.class){
            return Types.NUMERIC;
        } else if (javaType == byte[].class){
            return Types.BLOB;
        }
//        else if(javaType.isAssignableFrom(Reader.class)){
//            return Types.CLOB;
//        }
        //TODO more type!!!
        throw new RuntimeException("不支持的Jdbc类型转换："+javaType);
    }

    public static Class<?> javaType(int jdbcType){
        if(jdbcType == Types.VARCHAR || jdbcType == Types.CHAR || jdbcType == Types.LONGVARCHAR || jdbcType == Types.NVARCHAR || jdbcType == Types.NCHAR || jdbcType == Types.CLOB || jdbcType == Types.NCLOB){
            return String.class;
        } else if(jdbcType == Types.INTEGER || jdbcType == Types.TINYINT  || jdbcType == Types.SMALLINT || jdbcType == Types.BIT){
            return Integer.class;
        } else if(jdbcType == Types.TIMESTAMP || jdbcType == Types.DATE || jdbcType == Types.TIME){
            return Date.class;
        } else if (jdbcType == Types.BIGINT){
            return Long.class;
        } else if (jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL || jdbcType == Types.DOUBLE){
            return Double.class;
        } else if (jdbcType == Types.BLOB || jdbcType == Types.LONGVARBINARY || jdbcType == Types.BINARY){
            return byte[].class;
        }
//        else if(jdbcType == Types.CLOB || jdbcType == Types.NCLOB){
//            return Reader.class;
//        }
        //TODO more type!!!
        throw new RuntimeException("不支持的Java类型转换："+jdbcType);
    }
}
