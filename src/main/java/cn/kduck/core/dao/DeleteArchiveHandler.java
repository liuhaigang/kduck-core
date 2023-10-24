package cn.kduck.core.dao;

import java.util.List;
import java.util.Map;

/**
 * 删除数据归档接口，用于对被删除的数据进行处理。此接口在删除操作前，如果归档失败，则不会进行真正的删除操作。<p>
 * 如果应用未定制该接口实现，框架会启用了默认的实现，记录到k_delete_archive数据表中，表的结构参考开发手册的附录章节。
 * @author LiuHG
 */
public interface DeleteArchiveHandler {

    /**
     * 处理被删除的数据，需要特别处理被删除的大字段类型数据。
     * @param oid 操作标识，用于表示本次操作的唯一标识。
     * @param entityCode 被删除的实体定义对象编码
     * @param deletedRecords 被删除的记录集合。
     */
    void doArchive(String oid, String entityCode, List<Map<String, Object>> deletedRecords);
}
