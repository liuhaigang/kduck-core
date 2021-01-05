package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.exception.MissingAttributeException;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import cn.kduck.core.dao.sqlbuilder.template.update.UpdateFragmentTemplate;
import cn.kduck.core.dao.utils.JdbcUtils;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 更新语句构造器
 * @author LiuHG
 */
public class UpdateBuilder {

    private static final UpdateFragmentTemplate[] EMPTY_TEMPLATE = new UpdateFragmentTemplate[0];

    private SqlObject sqlObject;

    protected BeanEntityDef entityDef;
    private final UpdateFragmentTemplate[] fieldTemplate;

    protected Map<String, Object> paramMap;

    protected ConditionBuilder conditionBuilder;

    private List<String> requiredFieldList;

    public UpdateBuilder(BeanEntityDef entityDef, Map<String,Object> paramMap){
        this(entityDef,paramMap, EMPTY_TEMPLATE);
    }

    public UpdateBuilder(BeanEntityDef entityDef, Map<String,Object> paramMap, UpdateFragmentTemplate... fieldTemplate){
        this.paramMap = paramMap;
        this.entityDef = entityDef;
        this.fieldTemplate = fieldTemplate;
    }

    /**
     * 定义更新字段必须赋值的属性，避免属性拼写错误或业务漏洞导致字段未赋值导致更新问题。
     * 此处定义的属性名仅做存在性检测，不做值是否为null或空值的判断，因为允许开发者定义属性为null的值来将字段更新为null的情况
     * @param attrNames 必须赋值的属性名，仅做存在性检测，不针对值的内容做检测。
     * @return UpdateBuilder
     */
    public UpdateBuilder requiredFields(String... attrNames){
        if(attrNames.length == 0) return this;

        if(requiredFieldList == null){
            requiredFieldList = new ArrayList<>();
        }

        for (String attrName : attrNames) {
            BeanFieldDef fieldDef = entityDef.getFieldDef(attrName);
            if(fieldDef == null){
                throw new RuntimeException("指定的更新字段不存在，请确认属性名是否存在：" + attrName);
            }
            requiredFieldList.add(attrName);
        }
        return this;
    }

    public ConditionBuilder where(){
        conditionBuilder = new ConditionBuilder(){
            @Override
            protected boolean checkRequired(String attrName) {
                return checkPass(attrName);
            }
        };
        return conditionBuilder;
    }

    public ConditionBuilder where(String fieldName, ConditionType conditionType, String attrName){
        conditionBuilder = new ConditionBuilder(fieldName, conditionType, attrName) {
            @Override
            protected boolean checkRequired(String attrName) {
                return checkPass(attrName);
            }
        };
        return conditionBuilder;
    }

    private boolean checkPass(String attrName){
        Object value  = paramMap.get(attrName);
        if(StringUtils.isEmpty(value)){
            return false;
        }
        return true;
    }

    protected String toSql(){
        if(conditionBuilder == null){
            throw new RuntimeException("【违规范】更新必须包含更新条件");
        }

        if(requiredFieldList != null) {
            for (String attName : requiredFieldList) {
                if(!paramMap.containsKey(attName)){
                    throw new MissingAttributeException("指定的必填更新字段未赋值："+attName);
                }
            }
        }

        List<String> conditionAttrNames = conditionBuilder.getConditionAttrNames();

        String conditionSql = conditionBuilder.toCondition(paramMap,true);

        if(StringUtils.isEmpty(conditionSql)){
            throw new MissingAttributeException("【违规范】更新必须包含更新条件，虽然设置了可用更新条件：" + conditionAttrNames.toString() + "，但无一命中");
        }

        StringBuilder sqlBuidler = new StringBuilder("UPDATE ");
        sqlBuidler.append(entityDef.getTableName())
                .append(" SET ");

        List<BeanFieldDef> fieldList = entityDef.getFieldList();
        if(fieldList != null && !fieldList.isEmpty()){
            StringBuilder fieldFieldBuilder = new StringBuilder();
            for (BeanFieldDef fieldDef : fieldList) {
                UpdateFragmentTemplate fieldTemplate = getFieldTemplate(fieldDef.getAttrName());
                //判断如果paramMap中含有字段值并且不是条件属性，且该属性没有配置字段模版，则进行正常的更新逻辑
                if(paramMap.containsKey(fieldDef.getAttrName()) && !conditionAttrNames.contains(fieldDef.getAttrName()) && fieldTemplate == null){
                    fieldFieldBuilder.append(',')
                            .append(fieldDef.getFieldName())
                            .append(" = #{")
                            .append(fieldDef.getAttrName())
                            .append("}");
                }else if(fieldTemplate != null){
                    for (UpdateFragmentTemplate template : this.fieldTemplate) {
                        if(template.getAttrName().equals(fieldDef.getAttrName())){
                            fieldFieldBuilder.append(',')
                                    .append(template.buildFragment(fieldDef,paramMap));
                            break;
                        }
                    }
                }
            }
            String fileds = fieldFieldBuilder.toString();
            if(fileds.length() > 0){
                fileds = fileds.substring(1);
                sqlBuidler.append(fileds);
            }else{
                throw new MissingAttributeException("更新字段拼装失败，至少提供一个要更新的字段");
            }
        }

        sqlBuidler.append(conditionSql);

        return sqlBuidler.toString();
    }

    private UpdateFragmentTemplate getFieldTemplate(String attrName){
        if(fieldTemplate.length > 0){
            for (UpdateFragmentTemplate template : fieldTemplate) {
                if(template.getAttrName().equals(attrName)){
                    return template;
                }
            }
        }
        return null;

    }

    public SqlObject build(){

        if(sqlObject != null){
            return sqlObject;
        }

        String sql = toSql();

        List<String> conditionAttrNames = conditionBuilder.getConditionAttrNames();

        List<Object> conditionValueList = JdbcUtils.getValueList(conditionAttrNames, paramMap);

        List<BeanFieldDef> fieldList = entityDef.getFieldList();

        List<Object> allValueList = new ArrayList<>();
        List<String> nameList = JdbcUtils.getNameList(sql);
        for (String attrName : nameList) {
            //排除SQL语句的条件部分字段
            if(!isConditionAttrName(conditionAttrNames,attrName)){
                Object value = paramMap.get(attrName);
                BeanFieldDef fieldDef = entityDef.getFieldDef(attrName);
                allValueList.add(new SqlParameterValue(fieldDef.getJdbcType(),value));
            }
        }
        allValueList.addAll(conditionValueList);

        sql = sql.replaceAll(JdbcUtils.PLACEHOLDER_PATTERN,"?");

        //FIXME
        if(!(this instanceof DeleteBuilder) && fieldList.isEmpty()){
            throw new RuntimeException("拼接更新sql错误，更新字段列表为空。表名：" + entityDef.getTableName());
        }

        if(conditionValueList.isEmpty()){
            throw new MissingAttributeException("条件拼装失败，要求至少提供一个条件参数");
        }

        sqlObject = new SqlObject(sql, entityDef,fieldList, allValueList);

        return sqlObject;
    }

    private boolean isConditionAttrName(List<String> conditionAttrNames,String attrName){
        for (String conditionAttrName : conditionAttrNames) {
            if(conditionAttrName.equals(attrName) || attrName.startsWith(conditionAttrName + '_')){
                return true;
            }
        }
        return false;
    }

    public BeanEntityDef getEntityDef() {
        return entityDef;
    }
}
