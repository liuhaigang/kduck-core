package cn.kduck.core.dao;

import cn.kduck.core.dao.sqlbuilder.AliasField;

import java.util.List;

/**
 * 设置查询时返回的字段。
 * 一般在构造SelectBuilder的时候可以对返回的字段进行定义，此接口是在SelectBuilder定义的字段为最大范围进行二次字段过滤。
 * @author LiuHG
 */
@FunctionalInterface
public interface FieldFilter {

    List<AliasField> doFilter(List<AliasField> fieldList);
}
