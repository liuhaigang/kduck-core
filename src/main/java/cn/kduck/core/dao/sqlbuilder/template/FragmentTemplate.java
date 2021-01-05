package cn.kduck.core.dao.sqlbuilder.template;

import cn.kduck.core.dao.definition.BeanFieldDef;

import java.util.Map;

/**
 * SQL片段模版接口，用于一些特定而又常见场景。
 * @author LiuHG
 */
public interface FragmentTemplate {

    /**
     *
     * @param fieldDef 被定制的字段定义对象
     * @param paramMap 参数Map
     * @return SQL片段语句，可以使用"#{}"占位符方式代替参数
     */
    String buildFragment(BeanFieldDef fieldDef, Map<String, Object> paramMap);
}
