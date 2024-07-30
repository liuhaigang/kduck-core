package cn.kduck.core.dao;

import cn.kduck.core.dao.sqlbuilder.AliasField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LiuHG
 */
public class NameFieldFilter implements FieldFilter{

    private final String[] attrNames;

    public NameFieldFilter(String... attrNames){
        this.attrNames = attrNames;
    }

    @Override
    public List<AliasField> doFilter(List<AliasField> fieldList) {
        List<AliasField> filedList = new ArrayList<>();
        for (String attrName : attrNames) {
            for (AliasField field : fieldList) {
                String alias = field.getAlias();
                alias = alias == null ? field.getFieldDef().getAttrName(): alias;
                if(alias.equals(attrName)){
                    filedList.add(field);
                    break;
                }
            }
        }

        if(filedList.isEmpty()){
            Object[] values = fieldList.toArray(new AliasField[0]);
            throw new RuntimeException("没有匹配任何可用字段，请检查属性名拼写是否正确："+ Arrays.toString(values));
        }

        return filedList;
    }


}
