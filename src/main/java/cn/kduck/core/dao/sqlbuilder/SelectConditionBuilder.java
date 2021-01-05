package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.sqlbuilder.SelectConditionBuilder.OrderBuilder.OrderType;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Select操作的条件构造器
 * @author LiuHG
 */
public abstract class SelectConditionBuilder extends ConditionBuilder{

    private OrderBuilder orderBuilder;
    private GroupBuilder groupBuilder;

    SelectConditionBuilder(){}

    SelectConditionBuilder(String fieldName, ConditionType conditionType, String attrName){
        super(fieldName,conditionType,attrName);
    }

    public OrderBuilder orderBy(){
        if(orderBuilder == null){
            orderBuilder = new OrderBuilder();
        }
        return orderBuilder;
    }

    public DynamicOrderBuilder orderByDynamic(){
        if(orderBuilder == null){
            orderBuilder = new DynamicOrderBuilder();
        }
        return (DynamicOrderBuilder)orderBuilder;
    }

    public GroupBuilder groupBy(String... filedNames){
        if(groupBuilder == null){
            groupBuilder = new GroupBuilder(filedNames,orderBy());
        }
        return groupBuilder;
    }

    public SelectConditionBuilder and(String fieldName, ConditionType conditionType, String attrName,boolean required) {
        super.and(fieldName, conditionType, attrName,required);
        return this;
    }

    public SelectConditionBuilder or(String fieldName, ConditionType conditionType, String attrName,boolean required) {
        super.or(fieldName, conditionType, attrName,required);
        return this;
    }

    public SelectConditionBuilder notOr(String fieldName, ConditionType conditionType, String attrName,boolean required) {
        super.notOr(fieldName, conditionType, attrName,required);
        return this;
    }

    public SelectConditionBuilder notAnd(String fieldName, ConditionType conditionType, String attrName,boolean required) {
        super.notAnd(fieldName, conditionType, attrName,required);
        return this;
    }

    //################################ SUBQUERY #########################################

    @Override
    public SelectConditionBuilder and(String fieldName, ConditionType conditionType, QuerySupport query){
        super.and(fieldName,conditionType,query);
        return this;
    }

    @Override
    public SelectConditionBuilder or(String fieldName, ConditionType conditionType, QuerySupport query){
        super.or(fieldName,conditionType,query);
        return this;
    }

    @Override
    public SelectConditionBuilder notAnd(String fieldName, ConditionType conditionType, QuerySupport query){
        super.notAnd(fieldName,conditionType,query);
        return this;
    }

    @Override
    public SelectConditionBuilder notOr(String fieldName, ConditionType conditionType, QuerySupport query){
        super.notOr(fieldName,conditionType,query);
        return this;
    }

    //################################ SUBQUERY #########################################

    @Override
    public SelectConditionBuilder and(String fieldName, ConditionType conditionType, String attrName) {
        super.and(fieldName,conditionType,attrName);
        return this;
    }

    @Override
    public SelectConditionBuilder or(String fieldName, ConditionType conditionType, String attrName) {
        super.or(fieldName, conditionType, attrName);
        return this;
    }

    @Override
    public SelectConditionBuilder notOr(String fieldName, ConditionType conditionType, String attrName) {
        super.notOr(fieldName, conditionType, attrName);
        return this;
    }

    @Override
    public SelectConditionBuilder notAnd(String fieldName, ConditionType conditionType, String attrName) {
        super.notAnd(fieldName, conditionType, attrName);
        return this;
    }

    @Override
    protected String toCondition(Map<String, Object> valueMap,boolean needWhere){
        String sql = super.toCondition(valueMap,needWhere);
        SqlStringSplicer sqlBuidler = new SqlStringSplicer(sql);

        if(groupBuilder != null){
            String[] filedNames = groupBuilder.getFiledNames();
            if(filedNames.length > 0){
                sqlBuidler.append(" GROUP BY ");
                for (int i = 0; i < filedNames.length; i++) {
                    sqlBuidler.appendWrapped(filedNames[i]);
                    if(i+1 < filedNames.length){
                        sqlBuidler.append(',');
                    }
                }
            }
        }

        if(orderBuilder != null){
            Map<String, OrderType> orderMap = orderBuilder.getOrderMap(valueMap);
            Iterator<String> keys = orderMap.keySet().iterator();
            boolean first = true;
            while(keys.hasNext()){
                String fieldName = keys.next();
                OrderType orderType = orderMap.get(fieldName);
                if(first){
                    sqlBuidler.append(" ORDER BY");
                    first = false;
                }else{
                    sqlBuidler.append(',');
                }
                sqlBuidler.appendSpace()
                        .appendWrapped(fieldName)
                        .appendSpace()
                        .append(orderType);
            }
        }

        return sqlBuidler.toString();
    }

    public static class OrderBuilder{

        private Map<String,OrderType> orderMap = new LinkedHashMap<>();

        public OrderBuilder desc(String fieldName){
            orderMap.put(fieldName,OrderType.DESC);
            return this;
        }

        public OrderBuilder asc(String fieldName){
            orderMap.put(fieldName,OrderType.ASC);
            return this;
        }

        protected Map<String,OrderType> getOrderMap(Map<String, Object> valueMap){
            return orderMap;
        }

        public enum OrderType{
            ASC,DESC;
        }
    }

    /**
     * 动态排序，适用于列表头排序需求，如果动态排序和静态排序同时存在，则动态排序会在静态排序之后。
     */
    public static class DynamicOrderBuilder extends OrderBuilder{

        private Map<String,String> mappingMap = new LinkedHashMap<>();

        public DynamicOrderBuilder mapping(String fieldName,String paramName){
            mappingMap.put(paramName,fieldName);
            return this;
        }

        @Override
        protected Map<String,OrderType> getOrderMap(Map<String, Object> valueMap){
            if(!mappingMap.isEmpty()){
                Iterator<String> nameIterator = mappingMap.keySet().iterator();
                while(nameIterator.hasNext()){
                    String orderParamName = nameIterator.next();
                    Object orderParamValue = valueMap.get(orderParamName);
                    if(orderParamValue != null){
                        String orderType = orderParamValue.toString().toUpperCase();
                        if("DESC".equals(orderType)){
                            super.desc(mappingMap.get(orderParamName));
                        }else if("ASC".equals(orderType)){
                            super.asc(mappingMap.get(orderParamName));
                        }
                    }
                }
            }
            return super.getOrderMap(valueMap);
        }

    }

    public static class GroupBuilder {

        private final String[] filedNames;
        private final OrderBuilder orderBuilder;
        private HavingBuilder havingBuilder = new HavingBuilder();

        public GroupBuilder(String[] filedNames,OrderBuilder orderBuilder) {
            this.filedNames = filedNames;
            this.orderBuilder = orderBuilder;
        }

        String[] getFiledNames() {
            return filedNames;
        }

        public HavingBuilder having() {
            return havingBuilder;
        }

        public OrderBuilder orderBy(){
            return orderBuilder;
        }

    }

    public static class HavingBuilder {

    }

}
