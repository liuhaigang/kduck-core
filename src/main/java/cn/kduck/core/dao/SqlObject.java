package cn.kduck.core.dao;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;

import java.util.List;

/**
 * SQL及参数对象，包含要执行的SQL以及对应的参数值对象。
 * @author LiuHG
 */
public class SqlObject {
    private final String sql;

    private final String originalSql;

    private final BeanEntityDef entityDef;

    private List<BeanFieldDef> fieldDefList;

    private final List<Object> paramValueList;

    public SqlObject(String sql, BeanEntityDef entityDef,List<BeanFieldDef> fieldDefList, List<Object> paramValueList){
        this.sql = sql.trim();
        this.originalSql = this.sql;
        this.entityDef = entityDef;
        this.fieldDefList = fieldDefList;
        this.paramValueList = paramValueList;
    }

    public SqlObject(String sql, String originalSql,BeanEntityDef entityDef,List<BeanFieldDef> fieldDefList, List<Object> paramValueList){
        this.sql = sql.trim();
        this.originalSql = originalSql.trim();
        this.entityDef = entityDef;
        this.fieldDefList = fieldDefList;
        this.paramValueList = paramValueList;
    }

//    public SqlObject(String sql, BeanEntityDef entityDef, List<BeanFieldDef> fieldDefList, List<Object> paramValueList){
//        this.sql = sql.trim();
//        this.entityDef = entityDef;
//        this.fieldDefList = new ArrayList<>();
//        for (BeanFieldDef beanFieldDef: fieldDefList) {
//            this.fieldDefList.add(new AliasField(beanFieldDef));
//        }
//        this.paramValueList = paramValueList;
//    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParamValueList() {
        return paramValueList;
    }

    public List<BeanFieldDef> getFieldDefList() {
        return fieldDefList;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void setFieldDefList(List<BeanFieldDef> fieldDefList) {
        this.fieldDefList = fieldDefList;
    }

    public BeanEntityDef getEntityDef() {
        return entityDef;
    }
}
