package cn.kduck.core.dao.definition;

/**
 * 实体字段名称别名生成器
 * @author LiuHG
 * @see DefaultFieldAliasGenerator
 */
public interface FieldAliasGenerator {

    /**
     * 实体字段名称别名生成器
     * @param dataSourceKey 在多数据源模式下，区别不同数据源的标识，可能为null
     * @param table 表名
     * @param fieldName 字段名或列标签名，主要在查询时可能会有所区别：在查询时如果返回的字段并非实体中的字段，则返回标签名
     * @return 别名
     */
    String genAlias(String dataSourceKey,String table,String fieldName);
}
