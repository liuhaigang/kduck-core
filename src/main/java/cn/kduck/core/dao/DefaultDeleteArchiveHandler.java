package cn.kduck.core.dao;

import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.service.DefaultService;
import cn.kduck.core.service.ParamMap;
import cn.kduck.core.service.ValueBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * 使用注入的
 * Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: java.util.ArrayList[0]->org.springframework.util.LinkedCaseInsensitiveMap["join_date"])
 * LiuHG
 */
public class DefaultDeleteArchiveHandler extends DefaultService implements DeleteArchiveHandler, ApplicationContextAware {

    private ObjectMapper objectMapper;

    private static final String CODE_DELETE_ARCHIVE = "K_DELETE_ARCHIVE";
    
    @Override
    public void doArchive(String oid, String entityCode, List<Map<String, Object>> deletedRecords) {
        if(deletedRecords == null || deletedRecords.isEmpty())return;

        BeanEntityDef entityDef = super.getEntityDef(entityCode);

        String archiveData;
        try {
            archiveData = objectMapper.writeValueAsString(deletedRecords);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("删除归档数据转换json时发生错误",e);
        }

        Map<String, Object> paramMap = ParamMap.createAndSet("archiveData", archiveData)
                .set("tableName", entityDef.getTableName())
                .set("deleteDate", new Date())
                .set("archiveCode", oid).toMap();
        super.add(CODE_DELETE_ARCHIVE,paramMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        objectMapper = applicationContext.getBean(ObjectMapper.class);
    }
}
