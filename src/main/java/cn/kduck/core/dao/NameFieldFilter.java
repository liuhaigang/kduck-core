package cn.kduck.core.dao;

import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.exception.NoFieldMatchedException;
import cn.kduck.core.dao.sqlbuilder.AliasField;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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
            StringJoiner fieldNameJoiner = new StringJoiner(",");
            for (AliasField aliasField : fieldList) {
                String alias = aliasField.getAlias();
                BeanFieldDef fieldDef = aliasField.getFieldDef();
                fieldNameJoiner.add(alias == null ? fieldDef.getAttrName(): alias);
            }
            throw new NoFieldMatchedException("没有匹配任何可用字段，请检查属性名拼写是否正确："+ fieldNameJoiner);
        }

        return filedList;
    }


}
