package cn.kduck.core.dao;

import java.util.Map;

public interface BatchDataCallbackHandler {

    int batchSize();

    void processBatchData(Map<String, Object>[] recordMaps);
}
