package cn.kduck.core.dao.definition;

import cn.kduck.core.dao.exception.PrimaryKeyNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LiuHG
 */
public class BeanEntityDef {
    private final String namespace;
    private final String entityCode;
    private final String entityName;
    private final String tableName;
    private final List<BeanFieldDef> fieldList;

    private BeanEntityDef[] fkBeanEntityDef;

    private BeanFieldDef pkFieldDef;

    public BeanEntityDef(String namespace,String entityCode,String entityName,String tableName,List<BeanFieldDef> fieldList){
        this.namespace = namespace;
        this.entityCode = entityCode;
        this.entityName = entityName;
        this.tableName = tableName;
        this.fieldList = fieldList;
    }

    public BeanEntityDef(String tableName,List<BeanFieldDef> fieldList){
        this(null,tableName,tableName,tableName,fieldList);
    }

    public BeanEntityDef(String tableName){
        this(tableName,new ArrayList<>());
    }

    public boolean hasAttribute(String attrName){
        for (BeanFieldDef fieldDef:fieldList) {
            if(attrName.equals(fieldDef.getAttrName())){
                return true;
            }
        }
        return false;
    }

    public List<BeanFieldDef> getFieldList() {
        return Collections.unmodifiableList(fieldList);
    }

    public BeanFieldDef getFieldDef(String attrName) {
        for (BeanFieldDef fieldDef:fieldList) {
            if(attrName.equals(fieldDef.getAttrName())){
                return fieldDef;
            }
        }
        return null;
    }

    public BeanFieldDef getFieldDefByFieldName(String fieldName) {
        for (BeanFieldDef fieldDef:fieldList) {
            if(fieldName.equalsIgnoreCase(fieldDef.getFieldName())){
                return fieldDef;
            }
        }
        return null;
    }

    public BeanFieldDef getPkFieldDef() {
        if(pkFieldDef != null){
            return pkFieldDef;
        }
        for (BeanFieldDef fieldDef:fieldList) {
            if(fieldDef.isPk()){
                pkFieldDef = fieldDef;
                return fieldDef;
            }
        }
        throw new PrimaryKeyNotFoundException("对象定义中主键不存在："+ entityCode);
    }

    public String getTableName() {
        return tableName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public BeanEntityDef[] getFkBeanEntityDef() {
        return fkBeanEntityDef;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setFkBeanEntityDef(BeanEntityDef[] fkBeanEntityDef) {
        this.fkBeanEntityDef = fkBeanEntityDef;
    }

}
