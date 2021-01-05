package cn.kduck.core.dao.dialect;

public interface DatabaseDialect {

    String productName();

    String checkTable(String tableName);

    String pagingSql(String sql, int first, int maxRows);


}
