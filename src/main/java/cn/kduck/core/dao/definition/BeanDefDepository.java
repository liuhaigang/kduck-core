package cn.kduck.core.dao.definition;

import java.util.List;
import java.util.Map;

/**
 * @author LiuHG
 */
public interface BeanDefDepository {

    BeanEntityDef getEntityDef(String name);

    List<BeanFieldDef> getFieldDefList(String entityDefName);

    void deleteEntity(String name);

    void reloadEntity(String tableName);

    Map<String,BeanEntityDef> getAllEntityDef();

    void reloadAllEntity();
}
