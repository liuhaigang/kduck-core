package cn.kduck.core.utils;

import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.AliasField;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段定义工具类
 * @author  LiuHG
 */
public final class BeanDefUtils {

    private BeanDefUtils(){}

    public static AliasField getAliasField(List<AliasField> fieldList, String attrNames){
        for (AliasField field : fieldList) {
            BeanFieldDef fieldDef = field.getFieldDef();
            if(fieldDef.getAttrName().equals(attrNames)){
                return field;
            }
        }
        return null;
    }

    public static List<AliasField> includeAliasField(List<AliasField> fieldList, String... attrNames){
        List<AliasField> resultList = new ArrayList<>();
        for (int i = 0; i < fieldList.size(); i++) {
            AliasField aliasField = fieldList.get(i);
            BeanFieldDef beanFieldDef = aliasField.getFieldDef();
            for (String name : attrNames) {
                if(beanFieldDef.getAttrName().equals(name) && !resultList.contains(beanFieldDef)){
                    resultList.add(aliasField);
                }
            }
        }
        return resultList;
    }

    public static List<BeanFieldDef> includeField(List<BeanFieldDef> fieldList, String... attrNames){
//        return fieldList.stream().filter(f -> {
//            for (String name : attrNames) {
//                if(f.getAttrName().equals(name)){
//                    return true;
//                }
//            }
//            return false;
//        }).collect(Collectors.toList());

        List<BeanFieldDef> resultList = new ArrayList<>();
        for (int i = 0; i < fieldList.size(); i++) {
            BeanFieldDef beanFieldDef = fieldList.get(i);
            for (String name : attrNames) {
                if(beanFieldDef.getAttrName().equals(name) && !resultList.contains(beanFieldDef)){
                    resultList.add(beanFieldDef);
                }
            }
        }
        return resultList;
    }

    /**
     * 将提供的字段集合进行拼装，按照字段名判重，返回一个包含所有字段的集合，相同字段名的字段定义只会包含一个。
     * @param allFieldList
     * @return 返回一个包含所有字段的集合
     */
    public static List<BeanFieldDef> join(List<BeanFieldDef>... allFieldList){
        List<BeanFieldDef> resultFieldList = new ArrayList();
        for (List<BeanFieldDef> fieldList : allFieldList) {
            for (BeanFieldDef field : fieldList) {
                if(!existField(resultFieldList,field)){
                    resultFieldList.add(field);
                }
            }
        }
        return resultFieldList;
    }

    private static boolean existField(List<BeanFieldDef> fieldList ,BeanFieldDef field){
        for (BeanFieldDef f : fieldList){
            if(f.getFieldName().equals(field.getFieldName())){
                return true;
            }
        }
        return false;
    }

    public static List<AliasField> excludeAliasField(List<AliasField> aliasFieldList, String... excludeAttrNames){
        return aliasFieldList.stream().filter(f -> {
            String attrName = f.getFieldDef().getAttrName();
            return !StringUtils.contain(excludeAttrNames,attrName);
        }).collect(Collectors.toList());
    }

    public static List<BeanFieldDef> excludeField(List<BeanFieldDef> fieldList, String... excludeAttrNames){
        return fieldList.stream().filter(f -> {
            String attrName = f.getAttrName();
            return !StringUtils.contain(excludeAttrNames,attrName);
        }).collect(Collectors.toList());
    }

    public static BeanFieldDef getByColName(List<BeanFieldDef> fieldList, String columnName){
        for (BeanFieldDef fieldDef:fieldList) {
            if (fieldDef.getFieldName().equalsIgnoreCase(columnName)) {
                return fieldDef;
            }
        }
        return null;
    }

    public static BeanFieldDef getByAttrName(List<BeanFieldDef> fieldList, String attrName){
        for (BeanFieldDef fieldDef:fieldList) {
            if (fieldDef.getAttrName().equals(attrName)) {
                return fieldDef;
            }
        }
        return null;
    }
}
