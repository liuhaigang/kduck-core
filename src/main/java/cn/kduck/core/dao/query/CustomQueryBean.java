package cn.kduck.core.dao.query;

import cn.kduck.core.dao.FieldFilter;
import cn.kduck.core.service.ParamMap.Param;
import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.sqlbuilder.SignatureInfo;
import cn.kduck.core.dao.utils.JdbcUtils;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.query.formater.ValueFormatter;
import cn.kduck.core.dao.sqlbuilder.AliasField;
import cn.kduck.core.dao.sqlbuilder.SelectBuilder.AggregateType;
import cn.kduck.core.dao.sqlbuilder.SqlStringSplicer;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * LiuHG
 */
public class CustomQueryBean implements QuerySupport, SignatureInfo {

//    private static final String PLACEHOLDER_PATTERN = "#\\{([\\w]*)\\}";
//
//    private Pattern pattern = Pattern.compile(PLACEHOLDER_PATTERN);

    private final String querySql;
    private final Map<String,Object> paramMap;

    private final List<AliasField> fieldList;
    private final Map<String, List<AliasField>> fieldMap = new HashMap<>();

    private Map<String, AggregateType> aggregateMap = Collections.emptyMap();

    private Map<String, ValueFormatter> valueFormaterMap = new HashMap<>();

    private SqlObject sqlObject;

    private String generateBy;

    public CustomQueryBean(String querySql, Map<String,Object> paramMap, List<BeanFieldDef> fieldList){
        this.querySql = querySql;
        this.paramMap = paramMap;
        if(fieldList != null) {
            this.fieldList = new ArrayList<>(fieldList.size());
            for (BeanFieldDef fieldDef : fieldList){
                this.fieldList.add(new AliasField(fieldDef));
            }
        }else{
            this.fieldList = Collections.emptyList();
        }
    }

    public CustomQueryBean(String querySql, Param paramMap, List<BeanFieldDef> fieldList){
        this(querySql,paramMap.toMap(),fieldList);
    }

    public CustomQueryBean(String querySql, List<BeanFieldDef> fieldList){
        this(querySql,new HashMap<>(),fieldList);
    }

    public CustomQueryBean(String querySql){
        this(querySql,new HashMap<>(),null);
    }

    public CustomQueryBean(String querySql, Map<String,Object> paramMap){
        this(querySql,paramMap,new ArrayList<>());
    }

    public void bindFields(String alias, List<AliasField> fieldList){
        List<AliasField> beanFieldDefs = fieldMap.get(alias);
        if(beanFieldDefs == null){
            beanFieldDefs = new ArrayList<>();
            fieldMap.put(alias,beanFieldDefs);
        }
        for (AliasField aliasField : fieldList) {
            if(!existField(beanFieldDefs,aliasField)) {
                beanFieldDefs.add(aliasField);
            }
            if(!existField(this.fieldList,aliasField)) {
                this.fieldList.add(aliasField);
            }
        }
//        beanFieldDefs.addAll(fieldList);
//        this.fieldList.addAll(fieldList);
    }

    private boolean existField(List<AliasField> fieldList,AliasField aliasField){
        for (AliasField field : fieldList) {
            if(field.getFieldDef() == aliasField.getFieldDef()){
                return true;
            }
        }
        return false;
    }

    public void bindAggregate(Map<String, AggregateType> aggregateMap){
        if(aggregateMap == null) this.aggregateMap = Collections.emptyMap();
        this.aggregateMap = aggregateMap;
    }

    public void setParam(String name,Object value){
        paramMap.put(name,value);
    }

    public Object getParam(String name){
        return paramMap.get(name);
    }

    public void removeParam(String name){
        paramMap.remove(name);
    }

    @Override
    public SqlObject getQuery(FieldFilter filter) {
        List<AliasField> aliasFields;
        if(filter == null){
            aliasFields = fieldList;
        }else{
            if(fieldList == null || fieldList.isEmpty()){
                throw new RuntimeException("????????????????????????????????????????????????");
            }
            aliasFields = filter.doFilter(fieldList);
        }

        List<BeanFieldDef> fieldDefList = new ArrayList<>();
        aliasFields.forEach(item->{
            fieldDefList.add(item.getFieldDef());
        });

        if(sqlObject != null){
            sqlObject.setFieldDefList(fieldDefList);
            return sqlObject;
        }

        String sql = querySql;
        String queryFields;
//        List<BeanFieldDef> queryFieldDefs = new ArrayList<>(aliasFields.size());
        if(aliasFields != null && !aliasFields.isEmpty()){
            SqlStringSplicer filedBuilder = new SqlStringSplicer();
            for (AliasField aliasField : aliasFields) {
                String fieldAlias = aliasField.getAlias();//????????????????????????"a.USER_NAME AS userName"???"userName"??????
                BeanFieldDef fieldDef = aliasField.getFieldDef();

                String alias = getAlias(fieldDef);//?????????????????????????????????"a.USER_NAME"???"a."??????
                String fieldName = fieldDef.getFieldName();
                String field = alias + fieldName;
                //????????????????????????
                AggregateType aggregate = aggregateMap.get(field.toUpperCase());

                field = SqlStringSplicer.textWrapped(field);

                if(aggregate != null){
//                    String alias = field;
//
//                    int i = field.indexOf(".");
//                    if(i >= 0){
//                        alias = field.substring(i + 1);
//                    }

                    field = aggregate.formatAggregate(field);
                    fieldAlias = fieldAlias == null ? fieldName : fieldAlias;
                }
//                filedBuilder.append("," + field + (fieldAlias == null ? "": " AS " + fieldAlias));
                filedBuilder.append(',');
                filedBuilder.append(field);
                if(fieldAlias != null){
                    filedBuilder.append(" AS ");
                    filedBuilder.appendWrapped(fieldAlias);
                }
            }
            queryFields = filedBuilder.toString().substring(1);
        }else{
            queryFields = "*";
        }

        sql = sql.replaceFirst("\\{\\*\\}",queryFields);

//        List<Object> valueList = new ArrayList<>();
//
//        Matcher matcher = pattern.matcher(querySql);
//        while(matcher.find()) {
//            String placeholder = matcher.group(1);
//            Object value = paramMap.get(placeholder);
//            if(value == null){
//                throw new IllegalArgumentException("????????????"+placeholder+"????????????sql???" + querySql);
//            }
//            valueList.add(value);
//        }
        List<Object> valueList = JdbcUtils.getValueList(sql, paramMap);

        String originalSql = sql;
        sql = sql.replaceAll(JdbcUtils.PLACEHOLDER_PATTERN,"?");

        sqlObject = new SqlObject(sql,originalSql,null,fieldDefList,valueList);
        return sqlObject;
    }

    @Override
    public Map<String, ValueFormatter> getValueFormater() {
        return Collections.unmodifiableMap(valueFormaterMap);
    }

    @Override
    public Map<String, Object> getParamMap() {
        return Collections.unmodifiableMap(paramMap);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     * @param attrName
     * @param valueFormatter
     */
    public void addValueFormatter(String attrName, ValueFormatter valueFormatter) {
        this.valueFormaterMap.put(attrName, valueFormatter);
    }

    private String getAlias(BeanFieldDef fieldDef){
        if(fieldMap.isEmpty())return "";

        Iterator<String> keys = fieldMap.keySet().iterator();
        while(keys.hasNext()){
            String alias = keys.next();
            List<AliasField> beanFieldDefs = fieldMap.get(alias);
            if(contains(beanFieldDefs,fieldDef) && StringUtils.hasText(alias)){
                return alias + ".";
            }
        }
        return "";

    }

    private boolean contains(List<AliasField> aliasFieldList ,BeanFieldDef fieldDef){
        for (AliasField aliasField : aliasFieldList) {
            if(aliasField.getFieldDef() == fieldDef){
                return true;
            }
        }
        return false;
    }

    void setGenerateBy(String name){
        this.generateBy = name;
    }

    @Override
    public String generateBy() {
        return generateBy;
    }

//    private class AliasField{
//        private final String alias;
//        private final BeanFieldDef fieldDef;
//
//        public AliasField(BeanFieldDef fieldDef) {
//            this.alias = null;
//            this.fieldDef = fieldDef;
//        }
//
//        public AliasField(String alias, BeanFieldDef fieldDef) {
//            this.alias = alias;
//            this.fieldDef = fieldDef;
//        }
//
//        public String getAlias() {
//            return alias;
//        }
//
//        public BeanFieldDef getFieldDef() {
//            return fieldDef;
//        }
//    }
}
