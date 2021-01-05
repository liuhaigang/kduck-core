package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import org.springframework.jdbc.core.SqlParameterValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 插入语句构造器
 * @author LiuHG
 */
public class InsertBuilder {

    private final BeanEntityDef entityDef;

    private List<Map<String,Object>> paramMapList;

    public InsertBuilder(BeanEntityDef entityDef, List<Map<String,Object>> paramMapList){
        this.paramMapList = paramMapList;
        this.entityDef = entityDef;
    }

    protected String toSql(){
        List<BeanFieldDef> fieldList = entityDef.getFieldList();

        StringBuilder filedBuilder = new StringBuilder();
        StringBuilder placeholderBuilder = new StringBuilder();

        for (BeanFieldDef fieldDef : fieldList) {
            filedBuilder.append("," + fieldDef.getFieldName());
            placeholderBuilder.append(",?");
        }

        String fields = filedBuilder.toString().substring(1);
        String placeholder = placeholderBuilder.toString().substring(1);

        String insertSql = "INSERT INTO " + entityDef.getTableName() + "(" + fields + ")VALUES(" + placeholder + ")";

        return insertSql;
    }

    public SqlObject build(){
        List<BeanFieldDef> fieldList = entityDef.getFieldList();

        List<Object> valueList = new ArrayList();
        for (Map<String,Object> paramMap:paramMapList) {
            SqlParameterValue[] rowValue = new SqlParameterValue[fieldList.size()];
            int column = 0;
            for (BeanFieldDef fieldDef : fieldList) {
                rowValue[column++] = new SqlParameterValue(fieldDef.getJdbcType(),paramMap.get(fieldDef.getAttrName()));
            }
            valueList.add(rowValue);
        }

        return new SqlObject(toSql(),entityDef,fieldList,valueList);
    }
}
