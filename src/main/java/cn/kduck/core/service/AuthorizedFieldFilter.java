package cn.kduck.core.service;

import cn.kduck.core.dao.definition.BeanEntityDef;

import java.util.List;

/**
 * LiuHG
 */
public interface AuthorizedFieldFilter {

    List<String> doFieldFilter(String user, BeanEntityDef entityDef);
}
