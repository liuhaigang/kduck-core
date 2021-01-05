package cn.kduck.core.dao.query.formater;

import java.util.Map;

/**
 * LiuHG
 */
@FunctionalInterface
public interface ValueFormatter {

    Object format(Object value, Map<String,Object> valueMap);
}
