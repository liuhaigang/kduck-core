package cn.kduck.core.service;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.service.exception.AttributeNotExistException;
import cn.kduck.core.utils.ConversionUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class ValueBean {

    private final BeanEntityDef entityDef;
    private Map<String,Object> valueMap;

    private boolean strict;

    private final ValueBean parentValueBean;

    public ValueBean(BeanEntityDef entityDef){
        this(entityDef,null,null,true);
    }

    public ValueBean(BeanEntityDef entityDef,Map<String,Object> valueMap,boolean strict){
        this(entityDef,valueMap,null,strict);
    }

    public ValueBean(BeanEntityDef entityDef,ValueBean parentValueBean){
        this(entityDef,null,parentValueBean,true);
    }

    public ValueBean(BeanEntityDef entityDef,Map<String,Object> valueMap,ValueBean parentValueBean,boolean strict){
        this.entityDef = entityDef;
        this.parentValueBean = parentValueBean;
        this.strict = strict;
        this.valueMap = valueMap == null ? new HashMap<>() : valueMap;
        Assert.notNull(entityDef,"Bean定义必须指定，不能为null");

        if(valueMap != null){
            Iterator<String> keyIterator = valueMap.keySet().iterator();
            while(keyIterator.hasNext()){
                String name = keyIterator.next();
                if(isStrict()){
                    setValue(name,valueMap.get(name));
                }else{
                    if(hasAttr(name)){
                        setValue(name,valueMap.get(name));
                    }
                }
            }
        }
    }

    public ValueBean setIdValue(Object idValue){
        BeanFieldDef pkFieldDef = entityDef.getPkFieldDef();
        valueMap.put(pkFieldDef.getAttrName(), idValue);
        return this;
    }

    public Serializable getIdValue(){
        BeanFieldDef pkFieldDef = entityDef.getPkFieldDef();
        return (Serializable)valueMap.get(pkFieldDef.getAttrName());
    }

    public ValueBean setValue(String name,Object value){
        BeanFieldDef fieldDef = entityDef.getFieldDef(name);
        if(fieldDef == null){
            throw new AttributeNotExistException("setValue错误，在" + entityDef.getEntityCode() + "中的属性不存在:" + name);
        }
        Object convertValue = ConversionUtils.convert(value, fieldDef.getJavaType());
//        if(value.getClass().isArray()){
//            Object o = Array.newInstance(fieldDef.getJavaType(), 0);
//            convertValue = ConversionUtils.convert(value, o.getClass());
//        }else{
//            convertValue = ConversionUtils.convert(value, fieldDef.getJavaType());
//        }

        valueMap.put(name, convertValue);

        return this;
    }

    public Object getValue(String name){
        if(!entityDef.hasAttribute(name)){
            throw new AttributeNotExistException("getValue错误，在" + entityDef.getEntityCode() + "中的:" + name);
        }
        return valueMap.get(name);
    }

    public void clearValue(String name){
        if(!entityDef.hasAttribute(name)){
            throw new AttributeNotExistException("clearValue错误，在" + entityDef.getEntityCode() + "中的属性不存在:" + name);
        }
        valueMap.put(name,null);
    }

    public void removeField(String name){
        if(!entityDef.hasAttribute(name)){
            throw new AttributeNotExistException("removeField错误，在" + entityDef.getEntityCode() + "中属性不存在:" + name);
        }
        valueMap.remove(name);
    }

    public void removeAllField(){
        valueMap.clear();
    }

    public Map<String,Object> getValueMap(){
        return valueMap;
    }

    public ValueBean getParentValueBean() {
        return parentValueBean;
    }

    public BeanEntityDef getEntityDef() {
        return entityDef;
    }

    public boolean hasAttr(String attrName) {
        List<BeanFieldDef> fieldList = getEntityDef().getFieldList();
        for (BeanFieldDef fieldDef : fieldList) {
            if(fieldDef.getAttrName().equals(attrName)){
                return true;
            }
        }
        return false;
    }

    public boolean hasValue(String attrName) {
        return valueMap.containsKey(attrName);
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
