package cn.kduck.core.dao.definition;

import cn.kduck.core.dao.utils.TypeUtils;

/**
 * LiuHG
 */
public class BeanFieldDef {
    private String fieldName;
    private String attrName;
    private Class javaType;
    private int jdbcType;
    private boolean isPk;
    private String remarks;

    private DefaultValue defaultValue;//默认值

//    private TypeConverter typeConverter;//字段值转换器

    //TODO 字段校验器

    public BeanFieldDef(){}

    public BeanFieldDef(String attrName,String fieldName,Class javaType){
        this(attrName,fieldName,javaType,false);
    }

    public BeanFieldDef(String attrName,String fieldName,Class javaType,boolean isPk){
        this.attrName = attrName;
        this.fieldName = fieldName;
        this.isPk = isPk;
        this.javaType = javaType;
        this.jdbcType = TypeUtils.jdbcType(javaType);
    }

    public BeanFieldDef(String attrName,String fieldName,int jdbcType){
        this(attrName,fieldName,jdbcType,false);
    }

    public BeanFieldDef(String attrName,String fieldName,int jdbcType,boolean isPk){
        this.attrName = attrName;
        this.fieldName = fieldName;
        this.isPk = isPk;
        this.javaType = TypeUtils.javaType(jdbcType);
        this.jdbcType = jdbcType;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
    }

    public boolean isPk() {
        return isPk;
    }

    public void setPk(boolean pk) {
        isPk = pk;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
