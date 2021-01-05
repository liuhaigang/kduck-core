package cn.kduck.core.dao.field;

import cn.kduck.core.dao.FieldFilter;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.AliasField;

import java.util.ArrayList;
import java.util.List;

public class BeanFieldListFilter implements FieldFilter {

    private final List<AliasField> fieldList = new ArrayList<>();

    public BeanFieldListFilter(List<BeanFieldDef> beanFieldList){
        for (BeanFieldDef beanFieldDef : beanFieldList) {
            this.fieldList.add(new AliasField(beanFieldDef));
        }
    }

    @Override
    public List<AliasField> doFilter(List<AliasField> fieldList) {
        return this.fieldList;
    }
}
