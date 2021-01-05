package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.utils.JdbcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 通用的条件构造器
 * @author LiuHG
 */
public abstract class ConditionBuilder {

    private final Log logger = LogFactory.getLog(getClass());

    private final static int GROUP_SPLIT_SIZE = 1000;

    private final List<Condition> conditionList = new ArrayList();

    private ConditionBuilder parent;

    private Condition firstCondition = null;

    ConditionBuilder(){}

    ConditionBuilder(ConditionBuilder parent){
        this.parent = parent;
    }

    ConditionBuilder(String fieldName, ConditionType conditionType, String paramName){
        conditionList.add(new StandardCondition(fieldName,conditionType,paramName));
    }


    public ConditionBuilder and(String fieldName, ConditionType conditionType, String paramName,boolean required) {
        checkRequired(paramName,required);
        and(fieldName, conditionType, paramName);
        return this;
    }

    public ConditionBuilder or(String fieldName, ConditionType conditionType, String paramName,boolean required) {
        checkRequired(paramName,required);
        or(fieldName, conditionType, paramName);
        return this;
    }

    public ConditionBuilder notOr(String fieldName, ConditionType conditionType, String paramName,boolean required) {
        checkRequired(paramName,required);
        notOr(fieldName, conditionType, paramName);
        return this;
    }

    public ConditionBuilder notAnd(String fieldName, ConditionType conditionType, String paramName,boolean required) {
        checkRequired(paramName,required);
        notAnd(fieldName, conditionType, paramName);
        return this;
    }

    private void checkRequired(String attrName,boolean required){
        if(required && !checkRequired(attrName)){
            throw new IllegalArgumentException("查询条件必须包含属性：" + attrName);
        }
    }

    protected abstract boolean checkRequired(String attrName);


    public ConditionBuilder and(String fieldName, ConditionType conditionType, String attrName){
        conditionList.add(new StandardCondition(LogicType.AND,fieldName,conditionType,attrName));
        return this;
    }

    public ConditionBuilder or(String fieldName, ConditionType conditionType, String attrName){
        conditionList.add(new StandardCondition(LogicType.OR,fieldName,conditionType,attrName));
        return this;
    }

    public ConditionBuilder notOr(String fieldName, ConditionType conditionType, String attrName){
        conditionList.add(new StandardCondition(LogicType.NOT_OR,fieldName,conditionType,attrName));
        return this;
    }

    public ConditionBuilder notAnd(String fieldName, ConditionType conditionType, String attrName){
        conditionList.add(new StandardCondition(LogicType.NOT_AND,fieldName,conditionType,attrName));
        return this;
    }

    //################################ SUBQUERY #########################################

    public ConditionBuilder and(String fieldName, ConditionType conditionType, QuerySupport query){
        conditionList.add(new SubqueryCondition(LogicType.AND,fieldName,conditionType,query));
        return this;
    }

    public ConditionBuilder or(String fieldName, ConditionType conditionType, QuerySupport query){
        conditionList.add(new SubqueryCondition(LogicType.OR,fieldName,conditionType,query));
        return this;
    }

    public ConditionBuilder notAnd(String fieldName, ConditionType conditionType, QuerySupport query){
        conditionList.add(new SubqueryCondition(LogicType.NOT_OR,fieldName,conditionType,query));
        return this;
    }

    public ConditionBuilder notOr(String fieldName, ConditionType conditionType, QuerySupport query){
        conditionList.add(new SubqueryCondition(LogicType.NOT_AND,fieldName,conditionType,query));
        return this;
    }

    //################################ SUBQUERY #########################################

    public ConditionBuilder and(String fieldName, ConditionType conditionType){
        if(conditionType.hasValue()){
            throw new RuntimeException("不允许有值的条件类型调用此方法:"+conditionType);
        }
        conditionList.add(new StandardCondition(LogicType.AND,fieldName,conditionType,null));
        return this;
    }

    public ConditionBuilder or(String fieldName, ConditionType conditionType){
        if(conditionType.hasValue()){
            throw new RuntimeException("不允许有值的条件类型调用此方法:"+conditionType);
        }
        conditionList.add(new StandardCondition(LogicType.OR,fieldName,conditionType,null));
        return this;
    }

    public ConditionBuilder notOr(String fieldName, ConditionType conditionType){
        if(conditionType.hasValue()){
            throw new RuntimeException("不允许有值的条件类型调用此方法:"+conditionType);
        }
        conditionList.add(new StandardCondition(LogicType.NOT_OR,fieldName,conditionType,null));
        return this;
    }

    public ConditionBuilder notAnd(String fieldName, ConditionType conditionType){
        if(conditionType.hasValue()){
            throw new RuntimeException("不允许有值的条件类型调用此方法:"+conditionType);
        }
        conditionList.add(new StandardCondition(LogicType.NOT_AND,fieldName,conditionType,null));
        return this;
    }

    public ConditionBuilder groupBegin(String fieldName, ConditionType conditionType, String attrName){
        return groupBegin(LogicType.AND,fieldName,conditionType,attrName);
    }

    public ConditionBuilder groupBegin(LogicType logicType ,String fieldName, ConditionType conditionType, String attrName){
        ConditionBuilder conditionBuilder = getConditionBuilder();
        conditionBuilder.and(fieldName,conditionType,attrName);
        conditionList.add(new ConditionGroup(conditionBuilder,logicType));
        return conditionBuilder;
    }

    public ConditionBuilder groupEnd(){
        if(parent == null){
            logger.warn("SQL条件构造不规范：没有调用groupBegin或者过多调用了groupEnd方法");
            return this;
        }
        return parent;
    }

    private ConditionBuilder getConditionBuilder(){
        return new ConditionBuilder(this) {
            @Override
            protected boolean checkRequired(String attrName) {
                return ConditionBuilder.this.checkRequired(attrName);
            }
        };
    }

//        public ConditionBuilder and(String fieldName,ConditionType conditionType,QuerySupport getAttrName){
//            conditionList.batchAdd(new Condition(LogicType.AND,fieldName,conditionType,getAttrName));
//            return this;
//        }
//
//        public ConditionBuilder or(String fieldName,ConditionType conditionType,QuerySupport getAttrName){
//            conditionList.batchAdd(new Condition(LogicType.OR,fieldName,conditionType,getAttrName));
//            return this;
//        }
//
//        public ConditionBuilder notOr(String fieldName,ConditionType conditionType,QuerySupport getAttrName){
//            conditionList.batchAdd(new Condition(LogicType.NOT_OR,fieldName,conditionType,getAttrName));
//            return this;
//        }
//
//        public ConditionBuilder notAnd(String fieldName,ConditionType conditionType,QuerySupport getAttrName){
//            conditionList.batchAdd(new Condition(LogicType.NOT_AND,fieldName,conditionType,getAttrName));
//            return this;
//        }

    public boolean hasCondition(){
        return !conditionList.isEmpty();
    }

    /* 仅为update和delete使用 */
    public List<String> getConditionAttrNames(){
        List<String> attrNameList = new ArrayList<>();
        for (Condition condition : conditionList) {
            //########################## GROUP ################################
            if(condition instanceof ConditionGroup){
                ConditionGroup groupCondition = (ConditionGroup) condition;
                attrNameList.addAll(groupCondition.getConditionAttrNames());
                continue;
            }
            //##########################################################
            //########################## SUBQUERY ################################
            if(condition instanceof SubqueryCondition){
                SubqueryCondition subqueryCondition = (SubqueryCondition) condition;
                attrNameList.addAll(subqueryCondition.getConditionAttrNames());
                continue;
            }
            //##########################################################
            String attrName = condition.getAttrName();
            if(attrName != null){//因为is null 和is not null的条件关系，所以可能attrName为null
                attrNameList.add(attrName);
            }
        }
        return attrNameList;
    }

    protected String toCondition(Map<String, Object> valueMap,boolean needWhere){
        SqlStringSplicer sqlBuidler = new SqlStringSplicer();
        boolean first = true;
        for (Condition condition : conditionList) {
            //########################### GROUP ###############################
            if(condition instanceof ConditionGroup){
                ConditionGroup groupCondition = (ConditionGroup) condition;
                String groupSql = groupCondition.toCondition(valueMap);
                if(StringUtils.hasText(groupSql)){
                    if(sqlBuidler.hasText()){
                        sqlBuidler.append(groupCondition.getLogicType().getLogicOpt());
                    }else{
                        needWhere = false;
                        sqlBuidler.append(" WHERE ");
                    }
                    sqlBuidler.append("(");
                    sqlBuidler.append(groupSql);
                    sqlBuidler.append(") ");
                }
                continue;
            }
            //##########################################################
            //########################## SUBQUERY ################################
            if(condition instanceof SubqueryCondition){
                SubqueryCondition groupCondition = (SubqueryCondition) condition;
                String subquerySql = groupCondition.toCondition(valueMap);
                if(StringUtils.hasText(subquerySql)){
                    if(sqlBuidler.hasText()){
                        sqlBuidler.append(groupCondition.getLogicType().getLogicOpt());
                    }else{
                        needWhere = false;
                        sqlBuidler.append(" WHERE ");
                    }
                    sqlBuidler.append(subquerySql);
                }
                continue;
            }
            //##########################################################
            String attrName = condition.getAttrName();
            Object value = valueMap.get(attrName);

            ConditionType conditionType = condition.getConditionType();
            if(value != null && conditionType.isMultipleValue()){
                Object[] values = getMultipleValue(value);
                if(values.length == 0){
                    continue;
                }
            }

            boolean staticValue = false;
            if(conditionType.hasValue() && attrName.startsWith("${") && attrName.endsWith("}")){
                staticValue = true;
            }

            if(!StringUtils.isEmpty(value) || !conditionType.hasValue() || staticValue){
                if(first){
                    if(needWhere){
                        sqlBuidler.append(" WHERE ");
                    }else {
                        //如果原基础sql包含了WHERE，则默认以为会至少带1个条件，因此需要使用连接符
                        sqlBuidler.append(condition.getLogicType().getLogicOpt());
                    }
                    firstCondition = condition;
                    first = false;
                }else{
                    sqlBuidler.append(condition.getLogicType().getLogicOpt());
                }



                //是否是静态值
                if(staticValue){
                    sqlBuidler.appendWrapped(condition.getFieldName());
                    sqlBuidler.append(condition.getConditionType().getRelationOpt());
                    sqlBuidler.append(attrName.substring(2,attrName.length() - 1));//FIXME "2"即为"${"，即取${和}之间的部分
                    continue;
                }

                //是否是多值条件类型，比如IN、NOT IN
                if(conditionType.isMultipleValue()){
                    Object[] valueArray = getMultipleValue(value);
//                    if(value.getClass().isArray()) {
//                        valueArray = (Object[]) value;
//                    }else if(value instanceof Collection) {
//                        valueArray = ((Collection)value).toArray();
//                    }else{
//                        throw new IllegalArgumentException("构造Query失败，对于IN查询的参数对象必须为数组或Collection对象：" + value.getClass());
//                    }

                    /*
                      判断当前IN的元素数量是否超过GROUP_SPLIT_SIZE（1000），如果超过，则按照每1000个元素为一组进行OR条件拼写。
                     */
                    int lave = valueArray.length % GROUP_SPLIT_SIZE;//in参数满组剩余的数量，即使没有被整除的余数

                    //如果超过1000则为OR条件添加分组括号
                    // 因为多组要用OR条件拼写，因此需要为整体IN条件用括号包装起来作为一个条件整体
                    if(valueArray.length > GROUP_SPLIT_SIZE){
                        sqlBuidler.append('(');
                    }

                    int groupCount = 0;//当前的分组数

                    int maxIndex = valueArray.length;
                    for (int i = 0; i < maxIndex; i++) {
                        valueMap.put(attrName + "_" + i,valueArray[i]);

                        if((i+1) % GROUP_SPLIT_SIZE == 0){
                            sqlBuidler.append(condition.getCondition(groupCount * GROUP_SPLIT_SIZE, groupCount * GROUP_SPLIT_SIZE + GROUP_SPLIT_SIZE));
                            groupCount++;
                            if((i+1) < maxIndex){
                                sqlBuidler.append(" OR ");
                            }
                            logger.warn("当前查询SQL语句的IN条件元素已经达到" + (groupCount*GROUP_SPLIT_SIZE) + "，请考虑逻辑优化");
                        }

                    }

                    if(lave > 0){
                        int startIndex = groupCount * GROUP_SPLIT_SIZE;
                        sqlBuidler.append(condition.getCondition(startIndex,startIndex + lave));
                    }

                    //如果超过1000则为OR条件添加分组括号
                    if(valueArray.length > GROUP_SPLIT_SIZE){
                        sqlBuidler.append(')');
                    }
                }else{
                    sqlBuidler.append(condition.getCondition(0,0));
                    if(value != null){
                        valueMap.put(attrName,conditionType.getFormatedValue(value));
                    }
                }

            }
        }
        return sqlBuidler.toString();
    }

    private Object[] getMultipleValue(Object value){
        if(!StringUtils.hasText(value.toString())){
            return new Object[0];
        }
        if(value.getClass().isArray()) {
            return (Object[]) value;
        }else if(value instanceof Collection) {
            return ((Collection)value).toArray();
        }else {
            return new Object[]{value};
        }
    }

    /**
     * 分组条件对象，用于为组合条件提供支持，拼装类似"(x=v1 and y=v2)"的分组条件。
     * @author LiuHG
     */
    static class ConditionGroup extends StandardCondition {

        private final ConditionBuilder groupCondition;

        public ConditionGroup(ConditionBuilder groupCondition, LogicType logicType) {
            super(logicType,null,null,null);
            this.groupCondition = groupCondition;
        }

        List<Condition> getConditionList() {
            return groupCondition.getConditionList();
        }

//        public String toCondition(Map<String, Object> paramMap){
//            return groupCondition.toCondition(paramMap,false);
//        }

        public String toCondition(Map<String, Object> paramMap){
            String condition = groupCondition.toCondition(paramMap, false);
            condition = condition.trim();

            if(StringUtils.isEmpty(condition)){
                return condition;
            }

            String logicOpt = groupCondition.getFirstCondition().getLogicType().toString();//此处不要是用getLogicType().getLogicOpt()方法代替，因为该方法默认增加前后空格，所以会导致下面的substring多删除2个字节
            condition = condition.substring(logicOpt.length()+1);

            return condition;
        }

        public List<String> getConditionAttrNames(){
            return groupCondition.getConditionAttrNames();
        }

    }

    List<Condition> getConditionList() {
        return conditionList;
    }


    /**
     *
     * @return
     */
    private Condition getFirstCondition() {
        return firstCondition;
    }

//    private void setFirstCondition(Condition firstCondition) {
//        if(this.firstCondition == null){
//            this.firstCondition = firstCondition;
//        }
//    }

    interface Condition {
        LogicType getLogicType();
        String getFieldName();
        ConditionType getConditionType();
        String getAttrName();

        String getCondition(int from,int to);

    }


    /**
     * 条件对象，用于包装单个条件。通过getCondition方法得到条件的SQL片段，并将参数名的部分拼装成"#{getAttrName}"的形式。
     * @author LiuHG
     */
    private static class StandardCondition implements Condition {

        private final String fieldName;
        private final ConditionType conditionType;
        private final String attrName;
        private final LogicType logicType;

        public StandardCondition(String fieldName, ConditionType conditionType, String attrName) {
            this.fieldName = fieldName;
            this.conditionType = conditionType;
            this.attrName = attrName;
            logicType = LogicType.AND;
        }

        public StandardCondition(LogicType logicType, String fieldName, ConditionType conditionType, String attrName) {
            this.fieldName = fieldName;
            this.conditionType = conditionType;
            this.attrName = attrName;
            this.logicType = logicType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public ConditionType getConditionType() {
            return conditionType;
        }

        public String getAttrName() {
            return attrName;
        }

        public LogicType getLogicType() {
            return logicType;
        }

//        public String getCondition() {
//            return getCondition(0);
//        }
//
//        public String getCondition(int paramNum) {
//            return getCondition(0,paramNum);
//        }

        public String getCondition(int from,int to) {
            if(from > to){
                throw new IllegalArgumentException(from + ">" + to);
            }

            SqlStringSplicer sqlBuidler = new SqlStringSplicer();

            String relationOpt = formatRelationOpt(conditionType.getRelationOpt(), getFieldName());

            if(conditionType.hasField()){
                sqlBuidler.appendWrapped(getFieldName());
            }
            sqlBuidler.append(relationOpt);
            if(conditionType.hasValue()){
                if(to > 0){
                    sqlBuidler.append('(');
                    for (int i = from; i < to; i++) {
                        sqlBuidler.appendPlaceholder(getAttrName()+ "_" + i);
                        if(i+1 < to){
                            sqlBuidler.append(',');
                        }
                    }
                    sqlBuidler.append(')');
                }else{
                    sqlBuidler.appendPlaceholder(getAttrName());
                }
            }
            return sqlBuidler.toString();
        }

        private String formatRelationOpt(String relationopt,String fieldName){
            return String.format(relationopt,fieldName);
        }
    }


    private static class SubqueryCondition implements Condition {

        private final String fieldName;
        private final ConditionType conditionType;
        private final SqlObject sqlObject;
        private final LogicType logicType;
        private final Map<String, Object> paramMap;

        public SubqueryCondition(String fieldName, ConditionType conditionType, QuerySupport query) {
            this.fieldName = fieldName;
            this.conditionType = conditionType;
            this.logicType = LogicType.AND;
            this.sqlObject = query.getQuery(null);
            this.paramMap = query.getParamMap();
        }

        public SubqueryCondition(LogicType logicType,String fieldName, ConditionType conditionType, QuerySupport query) {
            this.fieldName = fieldName;
            this.conditionType = conditionType;
            this.logicType = logicType;
            this.sqlObject = query.getQuery(null);
            this.paramMap = query.getParamMap();
        }

        public List<String> getConditionAttrNames(){
            return JdbcUtils.getNameList(sqlObject.getOriginalSql());
        }

        public String toCondition(Map<String, Object> paramMap){
            //采用合并参数，只将不存在的添加到参数paramMap中。原始的不会被覆盖。
//            Iterator<String> keys = this.paramMap.keySet().iterator();
//            while(keys.hasNext()){
//                String paramName = keys.next();
//                if(!paramMap.containsKey(paramName)){
//                    paramMap.put(paramName,this.paramMap.get(paramName));
//                }
//            }
            //采用覆盖参数，将子查询的覆盖掉原始的。
//            paramMap.putAll(this.paramMap);

            //采用有效参数覆盖，将子查询中用到的参数覆盖掉原始的。
            List<String> conditionAttrNames = getConditionAttrNames();
            for (String conditionAttrName : conditionAttrNames) {
                paramMap.put(conditionAttrName,this.paramMap.get(conditionAttrName));
            }

            SqlStringSplicer sqlBuidler = new SqlStringSplicer();
            sqlBuidler.appendWrapped(fieldName)
                    .append(conditionType.getRelationOpt())
                    .appendGroup(sqlObject.getOriginalSql());
            return sqlBuidler.toString();
        }

        public String getCondition(int from,int to) {
            throw new UnsupportedOperationException("子查询不支持该方法");
        }

        @Override
        public LogicType getLogicType() {
            return logicType;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public ConditionType getConditionType() {
            return conditionType;
        }

        @Override
        public String getAttrName() {
            return null;
        }
    }

    public enum ConditionType {
        EQUALS("="),
        LESS("<"),
        LESS_OR_EQUALS("<="),
        GREATER(">"),
        GREATER_OR_EQUALS(">="),
        CONTAINS("LIKE"),
        NOT_CONTAINS("NOT LIKE"),
        BEGIN_WITH("LIKE"),
        NOT_BEGIN_WITH("NOT LIKE"),
        END_WITH("LIKE"),
        NOT_END_WITH("NOT LIKE"),
        NOT_EQUALS("!="),
        IN("IN",true,true),
        NOT_IN("NOT IN",true,true),
        IS_NULL("IS NULL",false,false),
        IS_NOT_NULL("IS NOT NULL",false,false),
        IS_BLANK(" = \'\'",false,false),
        IS_NOT_BLANK(" != \'\'",false,false),
        IS_EMPTY("(%1$s IS NULL OR %1$s =\'\')",false,false,false),
        IS_NOT_EMPTY("(%1$s IS NOT NULL AND %1$s !=\'\')",false,false,false);

        private final String relationOpt;
        private final boolean hasField;
        private final boolean hasValue;
        private final boolean multipleValue;

        ConditionType(String relationOpt){
            this.relationOpt = relationOpt;
            this.hasField = true;
            this.hasValue = true;
            this.multipleValue = false;
        }

        ConditionType(String relationOpt,boolean hasValue,boolean multipleValue){
            this.relationOpt = relationOpt;
            this.hasField = true;
            this.hasValue = hasValue;
            this.multipleValue = multipleValue;
        }

        ConditionType(String relationOpt,boolean hasField,boolean hasValue,boolean multipleValue){
            this.relationOpt = relationOpt;
            this.hasField = hasField;
            this.hasValue = hasValue;
            this.multipleValue = multipleValue;
        }

        public String getRelationOpt(){
            return " " + relationOpt + " ";
        }

        public boolean hasValue() {
            return hasValue;
        }
        public boolean isMultipleValue() {
            return multipleValue;
        }

        public boolean hasField() {
            return hasField;
        }

        public Object getFormatedValue(Object value){
            if(!(value instanceof String)){
                return value;
            }
            value = StringUtils.replace(value.toString(),"%","");
            switch (this){
                case CONTAINS :  return "%" + value + "%";
                case BEGIN_WITH :  return value + "%";
                case END_WITH :  return "%" + value;
                case NOT_CONTAINS :  return "%" + value + "%";
                case NOT_BEGIN_WITH :  return value + "%";
                case NOT_END_WITH :  return "%" + value;
            }
            return value;
        }
    }

    public enum LogicType {

        AND("AND"),OR("OR"),NOT_AND("NOT AND"),NOT_OR("NOT OR");

        private final String logicOpt;

        LogicType(String logicOpt){
            this.logicOpt = logicOpt;
        }

        public String getLogicOpt() {
            return " " + logicOpt + " ";
        }
    }
}
