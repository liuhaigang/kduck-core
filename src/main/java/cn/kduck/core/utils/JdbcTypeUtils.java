package cn.kduck.core.utils;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class JdbcTypeUtils {
    private static final Map<String, Integer> typeMapping = new HashMap<>();
    private static final Map<Integer, Class<?>> typeClassMapping = new HashMap<>();

    static {

        typeClassMapping.put(Types.BIT, Boolean.class);
        typeClassMapping.put(Types.TINYINT, Byte.class);
        typeClassMapping.put(Types.SMALLINT, Short.class);
        typeClassMapping.put(Types.INTEGER, Integer.class);
        typeClassMapping.put(Types.BIGINT, Long.class);
        typeClassMapping.put(Types.FLOAT, Float.class);
        typeClassMapping.put(Types.REAL, Float.class);
        typeClassMapping.put(Types.DOUBLE, Double.class);
        typeClassMapping.put(Types.NUMERIC, java.math.BigDecimal.class);
        typeClassMapping.put(Types.DECIMAL, java.math.BigDecimal.class);
        typeClassMapping.put(Types.CHAR, String.class);
        typeClassMapping.put(Types.VARCHAR, String.class);
        typeClassMapping.put(Types.LONGVARCHAR, String.class);
        typeClassMapping.put(Types.DATE, java.sql.Date.class);
        typeClassMapping.put(Types.TIME, java.sql.Time.class);
        typeClassMapping.put(Types.TIMESTAMP, java.sql.Timestamp.class);
        typeClassMapping.put(Types.BINARY, byte[].class);
        typeClassMapping.put(Types.VARBINARY, byte[].class);
        typeClassMapping.put(Types.LONGVARBINARY, byte[].class);
        typeClassMapping.put(Types.BLOB, java.sql.Blob.class);
        typeClassMapping.put(Types.CLOB, java.sql.Clob.class);
        typeClassMapping.put(Types.ARRAY, java.sql.Array.class);
        typeClassMapping.put(Types.STRUCT, java.sql.Struct.class);
        typeClassMapping.put(Types.REF, java.sql.Ref.class);
        typeClassMapping.put(Types.DATALINK, java.net.URL.class);
        typeClassMapping.put(Types.BOOLEAN, Boolean.class);
        typeClassMapping.put(Types.NCHAR, String.class);
        typeClassMapping.put(Types.NVARCHAR, String.class);
        typeClassMapping.put(Types.LONGNVARCHAR, String.class);
        typeClassMapping.put(Types.NCLOB, java.sql.NClob.class);
        typeClassMapping.put(Types.ROWID, java.sql.RowId.class);
        typeClassMapping.put(Types.TIME_WITH_TIMEZONE, java.time.OffsetTime.class);
        typeClassMapping.put(Types.TIMESTAMP_WITH_TIMEZONE, java.time.OffsetDateTime.class);
        typeClassMapping.put(Types.OTHER, Object.class);

        typeMapping.put("CHAR", Types.CHAR);
        typeMapping.put("VARCHAR", Types.VARCHAR);
        typeMapping.put("TEXT", Types.VARCHAR);
        typeMapping.put("BOOLEAN", Types.BOOLEAN);
        typeMapping.put("BIT", Types.BIT);
        typeMapping.put("TINYINT", Types.TINYINT);
        typeMapping.put("SMALLINT", Types.SMALLINT);
        typeMapping.put("INTEGER", Types.INTEGER);
        typeMapping.put("BIGINT", Types.BIGINT);
        typeMapping.put("REAL", Types.REAL);
        typeMapping.put("FLOAT", Types.FLOAT);
        typeMapping.put("DOUBLE", Types.DOUBLE);
        typeMapping.put("DECIMAL", Types.DECIMAL);
        typeMapping.put("NUMERIC", Types.NUMERIC);
        typeMapping.put("DATE", Types.DATE);
        typeMapping.put("TIME", Types.TIME);
        typeMapping.put("TIMESTAMP", Types.TIMESTAMP);
        typeMapping.put("BINARY", Types.BINARY);
        typeMapping.put("VARBINARY", Types.VARBINARY);
        typeMapping.put("BLOB", Types.BLOB);
        typeMapping.put("CLOB", Types.CLOB);
        typeMapping.put("UUID", Types.OTHER);
        typeMapping.put("XML", Types.SQLXML);
        typeMapping.put("JSON", Types.OTHER);
        // 以下并不是标准的jdbc类型
        typeMapping.put("DATETIME",Types.TIMESTAMP);
    }

    /**
     * 将数据库字段类型名称转换为对应的 Types 常量值
     * @param dbType 数据库字段类型名称
     * @return Types 常量值
     */
    public static int getJdbcType(String dbType) {
        return typeMapping.getOrDefault(dbType.toUpperCase(), Types.OTHER);
    }

    public static Class<?> getJavaType(int jdbcType) {
        return typeClassMapping.get(jdbcType);
    }
}
