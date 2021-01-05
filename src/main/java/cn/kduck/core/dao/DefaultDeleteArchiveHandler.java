package cn.kduck.core.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.service.DefaultService;
import cn.kduck.core.service.ValueBean;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class DefaultDeleteArchiveHandler extends DefaultService implements DeleteArchiveHandler{

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String CODE_DELETE_ARCHIVE = "K_DELETE_ARCHIVE";

//    private boolean ignoreLobField = true;

    @Override
    public void doArchive(String oid, BeanEntityDef entityDef, List<Map<String, Object>> deletedRecords) {
        if(deletedRecords == null || deletedRecords.isEmpty())return;

        List<BeanFieldDef> fieldList = entityDef.getFieldList();

//        if(isIgnoreLobField()){
//            List<String> lobFields = new ArrayList();
//            for (BeanFieldDef beanFieldDef : fieldList) {
//                int jdbcType = beanFieldDef.getJdbcType();
//                if(jdbcType == Types.LONGVARCHAR || jdbcType == Types.LONGNVARCHAR || jdbcType == Types.LONGVARBINARY){
//                    lobFields.add(beanFieldDef.getFieldName());
//                }
//            }
//
//            if(!lobFields.isEmpty()){
//                for (Map<String, Object> deletedRecord : deletedRecords) {
//                    Iterator<String> keys = deletedRecord.keySet().iterator();
//                    while(keys.hasNext()){
//                        String attrName = keys.next();
//                        if(isLobField(lobFields,attrName) && deletedRecord.get(attrName) != null){
//                            deletedRecord.put(attrName, "<LOB>");
//                            break;
//                        }
//                    }
//                }
//            }
//        }

        String archiveData;
        try {
            archiveData = objectMapper.writeValueAsString(deletedRecords);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("删除归档数据转换json时发生错误",e);
        }

        ValueBean valueBeans = super.createValueBean(CODE_DELETE_ARCHIVE);
        valueBeans.setValue("archiveData",archiveData)
            .setValue("tableName",entityDef.getTableName())
            .setValue("deleteDate",new Date())
            .setValue("archiveCode",oid);
//            .setValue("delNum",deletedRecords.size())
//            .setValue("userId",null)
//            .setValue("userName",null);
        initValue(valueBeans);
        super.add(valueBeans);
    }

//    private boolean isLobField(List<String> lobFields,String attrName){
//        for (String lobField : lobFields) {
//            if(lobField.equals(attrName)){
//                return true;
//            }
//        }
//        return false;
//    }

    protected void initValue(ValueBean valueBean){}
}
