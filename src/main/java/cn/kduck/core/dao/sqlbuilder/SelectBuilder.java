package cn.kduck.core.dao.sqlbuilder;


import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.query.CustomQueryBean;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.Condition;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionGroup;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import cn.kduck.core.dao.sqlbuilder.JoinTable.JoinOn;
import cn.kduck.core.dao.sqlbuilder.QueryConditionDefiner.DefaultCondition;
import cn.kduck.core.utils.BeanDefUtils;
import cn.kduck.core.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 查询条件构造器
 * @author LiuHG
 */
public class SelectBuilder {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String FIELD_NAME_SPLIT_PATTERN = "[:]";

    private static final String SQL_HEAD = "SELECT {*} ";
    private static final String SQL_DISTINCT_HEAD = "SELECT DISTINCT {*} ";

    private final String sql;

    private String[] disableDefConditions;

    /**
     * 用于保存查询返回的字段，以：表别名-字段对象的结构保存，实时配置返回字段。
     */
    private final Map<String, List<AliasField>> fieldMap = new HashMap<>();
    /**
     * 用于保存查询返回的字段，以：表别名-字段属性名的结构保存，仅能在build()时构造返回字段。
     * 功能同fieldMap，但由于在build之前无法判断是否设置了表实体对象，无法提前确定字段实体。从开发角度为了便捷而存在。
     */
    private final Map<String, String[]> fieldAttrNameMap = new HashMap<>();
    private final Map<String, String> aliasFieldMap = new HashMap<>();

    private Map<String, Object> paramMap;
    private JoinTable joinTable;
    private SelectConditionBuilder conditionBuilder;

    private Map<String, AggregateType> aggregateMap = new HashMap<>();

    private boolean needWhere = true;

    public SelectBuilder(){
        this(Collections.emptyMap());
    }

    public SelectBuilder(Map<String,Object> paramMap){
        this(paramMap,false);
    }

    public SelectBuilder(BeanEntityDef entityDef){
        this(entityDef,null,false);
    }

    public SelectBuilder(Map<String,Object> paramMap, boolean distinct){
        this((distinct ? SQL_DISTINCT_HEAD : SQL_HEAD), paramMap,new ArrayList<>());
    }

    public SelectBuilder(BeanEntityDef entityDef,Map<String,Object> paramMap){
        this(entityDef, paramMap,false);
    }

    public SelectBuilder(BeanEntityDef entityDef, Map<String,Object> paramMap, boolean distinct){
        this(distinct ? SQL_DISTINCT_HEAD : SQL_HEAD, paramMap,entityDef.getFieldList());
        from("",entityDef);
    }

    /**
     *
     * @param sql
     * @deprecated
     */
    @Deprecated
    public SelectBuilder(String sql){
        this(sql,null,new ArrayList<>());
    }

    /**
     *
     * @param sql
     * @param fieldList
     * @deprecated
     */
    @Deprecated
    public SelectBuilder(String sql, List<BeanFieldDef> fieldList){
        this(sql,null,fieldList);
    }

    /**
     *
     * @param sql
     * @param paramMap
     * @deprecated
     */
    @Deprecated
    public SelectBuilder(String sql, Map<String,Object> paramMap){
        this(sql, paramMap,new ArrayList<>());
    }

    /**
     *
     * @param sql
     * @param paramMap
     * @param fieldList
     * @deprecated
     */
    @Deprecated
    public SelectBuilder(String sql, Map<String,Object> paramMap, List<BeanFieldDef> fieldList){
        this.sql = sql;
        if(!fieldList.isEmpty()){
            bindFields("",fieldList);
        }
        bindParamMap(paramMap);
    }

    /**
     * 多表查询时，指定返回的表字段。因为多表查询返回的字段会有所不同，依靠此方法来分别指定不同表返回的字段。
     * 使用该方法的前提是没有在构造时提供字段集合。<p/>
     * 对于表字段的返回，框架提供了{@link BeanDefUtils BeanDefUtils}工具类进行字段过滤:
     * <pre>
     *     BeanDefUtils.include(fields,"userName");  -- 仅返回userName对应的字段
     *     BeanDefUtils.exclude(fields,"userName");  -- 返回字段中除了userName对应的字段
     *     BeanDefUtils.join(fields1,fields2...);  -- 返回fields1+fields2的字段，进行字段名判重处理
     *
     *     注意这里参数都是属性名，不是数据表字段
     * <pre/>
     * @param alias 表别名
     * @param fields 表返回的字段定义集合
     * @return 构造器自身
     * @see BeanDefUtils BeanDefUtils
     */
    public SelectBuilder bindFields(String alias, List<BeanFieldDef> fields){
        List<AliasField> selectField = new ArrayList<>(fields.size());
        for (BeanFieldDef fieldDef : fields) {
            selectField.add(new AliasField(fieldDef));
        }
        fieldMap.put(alias,selectField);
        return this;
    }

    //暂不开放使用
//    /**
//     * 为开发时方便设置查询返回的字段而存在的方法，如果设置了此方法会覆盖{@link #bindFields(String, List)}的配置。
//     * @param alias 表别名
//     * @param attrNames 返回的字段属性名，如果需要设置别名。如果需要设置别名，则以属性名+":"+别名的格式设置，例如：name:userName
//     * @return SelectBuilder自身
//     */
//    public SelectBuilder bindFields(String alias, String... attrNames){
//        fieldAttrNameMap.put(alias,attrNames);
//        return this;
//    }
//
//    public SelectBuilder bindFields(boolean reverse,String alias, String... attrNames){
//        if(reverse){
//            alias = "!" + alias;
//        }
//        fieldAttrNameMap.put(alias,attrNames);
//        return this;
//    }

    public SelectBuilder bindAliasField(String alias, BeanFieldDef field,String fieldAlias){
        List<AliasField> selectField = fieldMap.get(alias);
        if(selectField != null){
            //判断当前别名字段中是否已经包含了要绑定别名的字段定义对象。如果包含，则设置别名，否则加入新的字段并设置别名
            boolean hasField = false;
            for(AliasField f : selectField){
                if(f.getFieldDef() == field){
                    f.setAlias(fieldAlias);
                    hasField = true;
                    break;
                }
            }
            if(!hasField){
                selectField.add(new AliasField(fieldAlias,field));
            }
        }else{
            selectField = new ArrayList<>();
            selectField.add(new AliasField(fieldAlias,field));
            fieldMap.put(alias,selectField);
        }
        return this;
    }

    /**
     * 从目前已经绑定的字段中配置字段别名，同时设置别名后返回的字段属性名也同样会变成别名
     * @param fieldName 字段名，如果多表需要带上别名部分，例如：a.USER_NAME
     * @param fieldAlias 字段别名，生成时会生成类似：a.USER_NAME AS userName
     * @return SelectBuilder本身
     */
    public SelectBuilder bindAlias(String fieldName,String fieldAlias){

        aliasFieldMap.put(fieldName,fieldAlias);

        int i = fieldName.indexOf(".");
        String aliasFieldName = fieldName;
        List<AliasField> aliasFieldList;
        if(i >= 0){
            String alias = fieldName.substring(0,i);
            aliasFieldName = fieldName.substring(i + 1);
            aliasFieldList = fieldMap.get(alias);
            if(aliasFieldList == null && aliasFieldList.isEmpty()) {
                throw new RuntimeException("绑定别名错误，别名对应的字段不存在：" + fieldName);
            }
        }else{
            String alias = "";
            if(fieldMap.size() == 1) {
                alias = fieldMap.keySet().stream().findFirst().get();
            }
            aliasFieldList = fieldMap.get(alias);
        }

        if(aliasFieldList != null){
            for (AliasField field : aliasFieldList){
                String fName = field.getFieldDef().getFieldName();
                if(fName.equals(aliasFieldName)){
                    field.setAlias(fieldAlias);
                    break;
                }
            }
        }else if(fieldAttrNameMap.isEmpty()){
            logger.warn("修改别名前需要先绑定字段："+fieldName);
        }


        return this;
    }

    /**
     * 绑定一个聚合函数字段
     * @param fieldName 聚合函数字段，如果多表需要带上别名部分，例如：a.USER_NAME
     * @param type 聚合函数类型
     * @return SelectBuilder
     * @see SelectBuilder.AggregateType AggregateType
     */
    public SelectBuilder bindAggregate(String fieldName,AggregateType type){
        aggregateMap.put(fieldName.toUpperCase(),type);
        return this;
    }

    /**
     * 重置参数值对象，会覆盖由构造器给定的参数值对象。
     * @param valueMap 查询条件参数值对象，如果为null，则表示清空所有查询条件值。
     */
    public void bindParamMap(Map<String,Object> valueMap){
        if(valueMap == null){
            this.paramMap = Collections.emptyMap();
        } else {
            this.paramMap = new HashMap<>(valueMap);
        }
    }

    /**
     * 根据指定的实体定义对象构造FROM部分的SQL语句段，以此方法构造的SQL意味着以Join方式来构造表查询关联。
     * 如果提供了主体SQL部分，则注意不要拼写FORM及以后的部分。
     * @param alias 别名
     * @param entityDef 实体定义对象
     * @return Join相关操作对象
     */
    public JoinTable from(String alias, BeanEntityDef entityDef){
//        boolean processField = fieldMap.isEmpty();
//        where();
//        joinTable = new JoinTable(alias,entityDef,this,processField);
//        if(processField){
//            bindFields(alias,entityDef.getFieldList());
//        }
        boolean hasBindField = fieldMap.size() == 1 && fieldMap.containsKey("");
        if(hasBindField){
            fieldMap.clear();
        }

        boolean processField = fieldMap.isEmpty() || hasBindField;

        where();
        joinTable = new JoinTable(alias,entityDef,this,processField);
        if(processField){
            bindFields(alias,entityDef.getFieldList());
        }
        return joinTable;
    }

    public SelectConditionBuilder where(){
        return where(true);
    }
    public SelectConditionBuilder where(boolean needWhere){
        this.needWhere = needWhere;
        conditionBuilder = new SelectConditionBuilder(){
            @Override
            protected boolean checkRequired(String attrName) {
                return checkPass(attrName);
            }
        };
        return conditionBuilder;
    }

    public SelectConditionBuilder get(){
        if(conditionBuilder == null){
            throw new RuntimeException("尚未构建ConditionBuilder，请先调用where方法构建");
        }
        return conditionBuilder;
    }

    public SelectConditionBuilder where(String fieldName, ConditionType conditionType, String attrName){
        conditionBuilder = new SelectConditionBuilder(fieldName, conditionType, attrName){
            @Override
            protected boolean checkRequired(String attrName) {
                return checkPass(attrName);
            }
        };
        return conditionBuilder;
    }

    public SelectConditionBuilder where(String fieldName, ConditionType conditionType, String attrName,boolean required){
        SelectConditionBuilder conditionBuilder = where(true);
        conditionBuilder.and(fieldName,conditionType,attrName,required);
        return conditionBuilder;
    }

    private boolean checkPass(String attrName){
        Object value  = paramMap.get(attrName);
        if(StringUtils.isEmpty(value)){
            return false;
        }
        return true;
    }

    private String toSql(){
//        if(conditionBuilder == null || !conditionBuilder.hasCondition()){
        if(conditionBuilder == null){
            if(sql == null || SQL_HEAD.equals(sql) || SQL_DISTINCT_HEAD.equals(sql)){
                throw new RuntimeException("缺少足够构建查询SQL的参数");
            }
            return sql;
        }

        SqlStringSplicer sqlBuidler = new SqlStringSplicer(sql);

        if(joinTable != null){
            List<JoinOn> joinOnList = joinTable.getJoinOnList();

            //处理默认查询条件定义
            processConditionDefiner(joinOnList);

            //joinOnList为空，说明当前查询为单表查询，没有额外的join表。
            if(joinOnList.isEmpty()){
                sqlBuidler.append(" FROM ");

                sqlBuidler.appendWrapped(joinTable.getMainEntityDef().getTableName());

                String alias = joinTable.getAlias();
                if(StringUtils.hasText(alias)){
                    sqlBuidler.appendSpace();
                    sqlBuidler.append(alias);
                }
            }else{
                boolean first = true;
                for (JoinOn joinOn : joinOnList) {
                    if(first){
                        sqlBuidler.append(" FROM ");
                        sqlBuidler.append(joinOn.getLeftTable());
                        first = false;
                    }
                    sqlBuidler.appendSpace();
                    sqlBuidler.append(joinOn.getType().getJoinSql());
                    sqlBuidler.appendSpace();
                    sqlBuidler.append(joinOn.getRightTable());
                    sqlBuidler.append(" ON ");
                    sqlBuidler.append(joinOn.getJoinCondition(paramMap));
                }
            }
        }

        String conditionSql = conditionBuilder.toCondition(paramMap,needWhere);
        sqlBuidler.append(conditionSql);

        //FIXME 避免重复调用toSql(因为在首次执行toSql时已经将参数进行格式化处理)，因此暂时用这种方式禁止修改map值
        paramMap = Collections.unmodifiableMap(paramMap);

        return sqlBuidler.toString();
    }


    /**
     * 禁用指定实体编码下的所有默认条件
     * @param entityCodes 实体编码，不能为null
     */
    public void setDisableDefaultCondition(String... entityCodes){
        Assert.notEmpty(entityCodes,"设置禁用默认条件的实体编码不能为空");
        disableDefConditions = entityCodes;
    }

    /**
     * 处理默认条件定义
     * @param joinOnList
     */
    private void processConditionDefiner(List<JoinOn> joinOnList) {
        List<QueryConditionDefiner> definerList = QueryConditionContext.getConditionDefiner();
        for (QueryConditionDefiner definer : definerList) {
            for (JoinOn joinOn : joinOnList) {
                BeanEntityDef entityDef = joinOn.getLeftEntityDef();
                String leftAlias = joinOn.getLeftAlias();
                processDefaultCondition(definer, entityDef, leftAlias+".");
            }
            processDefaultCondition(definer, joinTable.getMainEntityDef(), "");
        }
    }

    private void processDefaultCondition(QueryConditionDefiner definer, BeanEntityDef entityDef, String rightAlias) {
        if(entityDef.getEntityCode().equals(definer.tableCode())){

            //判断是否禁用了当前实体的默认条件
            if(disableDefConditions != null) {
                for (String disableEntityCode : disableDefConditions) {
                    if(entityDef.getEntityCode().equals(disableEntityCode)){
                        return;
                    }
                }
            }

            DefaultCondition defaultCondition = definer.conditionDefine(entityDef);
            String fieldName = rightAlias + defaultCondition.getFieldDef().getFieldName();

            //判断条件中是否已经存在默认条件涉及的条件字段，如果存在则不再添加
            boolean fieldExist = existConditionField(fieldName.toUpperCase(),conditionBuilder.getConditionList());
            if(fieldExist){
                return;
            }

            conditionBuilder.and(fieldName,
                    defaultCondition.getType(),defaultCondition.getParamName());
            if(!paramMap.containsKey(defaultCondition.getParamName()) || !defaultCondition.isAllowOverride()){
                //因为paramMap为空时，paramMap是只读Map，需要重新构造
                if(paramMap.isEmpty()){
                    paramMap = new HashMap<>();
                }
                paramMap.put(defaultCondition.getParamName(),defaultCondition.getDefaultValue());
            }
        }
    }

    private boolean existConditionField(String fieldName,List<Condition> conditionList) {
        for (Condition condition : conditionList) {
            if(condition instanceof ConditionGroup){
                if(existConditionField(fieldName,((ConditionGroup)condition).getConditionList())){
                    return true;
                }
            }else if(fieldName.equals(condition.getFieldName().toUpperCase()) && paramMap.containsKey(fieldName)){
                return true;
            }
        }
        return false;
    }

    public QuerySupport build(){
        if(joinTable == null && (SQL_HEAD.equals(sql) || SQL_DISTINCT_HEAD.equals(sql))){
            throw new RuntimeException("调用where前需要先使用form方法构造数据表");
        }

        processSelectFields();

        //转换查询条件值对应的属性类型
        if(conditionBuilder != null && joinTable != null) {
            List<Condition> conditionList = conditionBuilder.getConditionList();
            convertParamValue(conditionList);
        }

        CustomQueryBean queryBean = new CustomQueryBean(toSql(), paramMap);
        Iterator<String> alias = fieldMap.keySet().iterator();
        while (alias.hasNext()){
            String name = alias.next();
            List<AliasField> aliasField = fieldMap.get(name);
            if(aliasField != null && !aliasField.isEmpty()){
                queryBean.bindFields(name,aliasField);
            }
        }
//        if(!fieldMap.isEmpty()){
//            queryBean = new CustomQueryBean(toSql(), paramMap);
//            Iterator<String> alias = fieldMap.keySet().iterator();
//            while (alias.hasNext()){
//                String name = alias.next();
//                List<AliasField> aliasField = fieldMap.get(name);
//                if(aliasField != null && !aliasField.isEmpty()){
//                    queryBean.bindFields(name,aliasField);
//                }
//            }
//        }else{
////            queryBean = new CustomQueryBean(toSql(), paramMap,fieldList);
//            queryBean = new CustomQueryBean(toSql(), paramMap);
//            queryBean.bindFields("",fieldList);
//        }
        if(!aggregateMap.isEmpty()){
            queryBean.bindAggregate(aggregateMap);
        }

        return queryBean;
    }

    private void processSelectFields() {
        //如果设置了属性名方式的返回字段设置，则清空（禁用）字段对象方式的设置
        if(!fieldAttrNameMap.isEmpty()){
            fieldMap.clear();
        }
        Iterator<String> aliasNames = fieldAttrNameMap.keySet().iterator();
        while(aliasNames.hasNext()){
            String name = aliasNames.next();

            String[] fieldNames = fieldAttrNameMap.get(name);

            //判断是否取反字段，即仅取非指定的字段
            boolean reverse = name.startsWith("!");
            if(reverse){
                name = name.substring(1);
            }

            String tablePrefix = StringUtils.hasText(name) ? name + "." : name;

            BeanEntityDef beanDef = joinTable.getJoinEntityDef(name);
            if(beanDef != null){
                List<AliasField> aliasFieldList = new ArrayList<>();
                List<BeanFieldDef> fieldDefList = beanDef.getFieldList();

                //如果没设置具体的属性名，则表示查询时默认返回全部字段。
                if(fieldNames.length == 0 && !reverse) {
                    for (BeanFieldDef beanFieldDef : fieldDefList) {
                        String alias = aliasFieldMap.get(tablePrefix + beanFieldDef.getFieldName());
                        aliasFieldList.add(new AliasField(alias,beanFieldDef));
                    }
                }

                //分别对需要取反和取指定字段的情况设置返回字段对象
                //FIXME 优化这段逻辑
                if(!reverse) {
                    for (String fieldName : fieldNames) {
                        String[] nameSplit = fieldName.split(FIELD_NAME_SPLIT_PATTERN);
                        boolean exist = false;
                        for (BeanFieldDef beanFieldDef : fieldDefList) {
                            if(beanFieldDef.getAttrName().equals(nameSplit[0])) {
                                if(nameSplit.length > 1){
                                    aliasFieldList.add(new AliasField(nameSplit[1],beanFieldDef));
                                }else{
                                    String alias = aliasFieldMap.get(tablePrefix + beanFieldDef.getFieldName());
                                    aliasFieldList.add(new AliasField(alias,beanFieldDef));
                                }
                                exist = true;
                                break;
                            }
                        }
                        if(!exist) throw new IllegalArgumentException("指定查询的返回字段属性不存在："+nameSplit[0]);
                    }
                }else{
                    for (BeanFieldDef beanFieldDef : fieldDefList) {
                        boolean exist = false;
                        for (String fieldName : fieldNames) {
                            String[] nameSplit = fieldName.split(FIELD_NAME_SPLIT_PATTERN);
                            if(beanFieldDef.getAttrName().equals(nameSplit[0])) {
                                exist = true;
                                break;
                            }
                        }
                        if(!exist){
                            String alias = aliasFieldMap.get(tablePrefix + beanFieldDef.getFieldName());
                            aliasFieldList.add(new AliasField(alias,beanFieldDef));
                        }
                    }
                }

                fieldMap.put(name, aliasFieldList);
            }else{
                throw new RuntimeException("当前查询中不存在别名为" + name + "的数据实体");
            }
        }
    }

    private void convertParamValue(List<Condition> conditionList) {
        for (Condition condition : conditionList) {
            if(condition instanceof ConditionGroup) {
                List<Condition> groupConditionList = ((ConditionGroup) condition).getConditionList();
                convertParamValue(groupConditionList);
            }

            //FIXME 需要在此处就对属性存在性进行检测，不能只检测有属性值的

            Object value = paramMap.get(condition.getAttrName());
            if(value != null){
                String fieldName = condition.getFieldName();
                String[] aliasNameSplit = fieldName.split("[.]");

                if(joinTable.getJoinOnList().size() > 0 && aliasNameSplit.length <= 1){
                    logger.warn("【违规范】此查询为多表关联，但是未对" + condition.getAttrName() + "属性对应的字段指定别名：" + fieldName + "，无法进行类型转换。");
                    continue;
                }

                List<BeanFieldDef> fieldDefs;
                String actualFieldName;
                if(aliasNameSplit.length > 1) {
                    fieldDefs = joinTable.getJoinEntityDef(aliasNameSplit[0]).getFieldList();
                    actualFieldName = aliasNameSplit[1];
                }else{
                    fieldDefs = joinTable.getJoinEntityDef("").getFieldList();
                    actualFieldName = fieldName;
                }

                for (BeanFieldDef fieldDef : fieldDefs) {
                    if(actualFieldName.equalsIgnoreCase(fieldDef.getFieldName())){
                        Class<?> valueType = value.getClass();
                        if(!fieldDef.getJavaType().isAssignableFrom(valueType) && !(value instanceof Collection)){
                            Class finalType = getParamType(fieldDef, valueType);
                            paramMap.put(condition.getAttrName(), ConversionUtils.convert(value,finalType));
                        }
                        break;
                    }
                }
                //TODO 循环后，如果没有任何字段匹配，说明条件字段名写错了，应该抛异常。但考虑到有项目已经在条件字段名套用了函数，因此暂时不实现该功能
            }
        }
    }

    /**
     * 自动转换查询条件中的参数类型，主要是处理数组的情况
     * @param fieldDef
     * @param valueType
     * @return
     */
    private Class getParamType(BeanFieldDef fieldDef, Class<?> valueType) {
        Class finalType;
        if(valueType.isArray()){
            finalType = Array.newInstance(fieldDef.getJavaType(), 0).getClass();
        }else{
            finalType = fieldDef.getJavaType();
        }
        return finalType;
    }

    public enum AggregateType {
        COUNT("COUNT(%s)"),
        COUNT_ALL("COUNT(*)"),
        COUNT_DISTINCT("COUNT(DISTINCT %s)"),
        SUM("SUM(%s)"),
        AVG("AVG(%s)"),
        MAX("MAX(%s)"),
        MIN("MIN(%s)");

        private final String template;

        AggregateType(String template){
            this.template = template;
        }

        public String formatAggregate(String fieldName){
            return String.format(template,fieldName);
        }
    }


}
