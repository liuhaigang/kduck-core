package cn.kduck.core.dao.query;

import cn.kduck.core.dao.FieldFilter;
import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.query.formater.ValueFormatter;
import cn.kduck.core.service.ValueMap;

import java.util.Map;

/**
 *
 * LiuHG
 */
public interface QuerySupport {
    /**
     * 需要在处理返回的Query对象，如果已经被处理，则返回同一个Query，避免在反复调用该方法时，重复拼装条件。
     * 但字段过滤逻辑需要在返回Query之前处理
     * @param filter
     * @return SqlObject
     */
    SqlObject getQuery(FieldFilter filter);

    Map<String, ValueFormatter> getValueFormater();

    /**
     * 返回参数值Map对象，要求返回只读Map
     * @return Map
     */
    Map<String,Object> getParamMap();

    /**
     * 为查询后的字段值进行格式化
     * @param attrName 预格式化的属性名
     * @param valueFormatter 格式化器
     * @see ValueMap#formatValue(String, ValueFormatter)
     */
    void addValueFormatter(String attrName, ValueFormatter valueFormatter);

    /**
     * 当前Query的创建者，一般用于返回创建的Query的class类名，输出到sql输出中。
     * @return
     */
    String generateBy();
}
