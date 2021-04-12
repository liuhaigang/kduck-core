package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 查询SQL中负责表Join关联的条件对象，目前支持innerJoin、leftJoin和rightJoin。
 * @author LiuHG
 */
public class JoinTable {

    private String alias;
    private BeanEntityDef mainEntityDef;
//    private final SelectConditionBuilder selectConditionBuilder;
    private final SelectBuilder selectBuilder;
    private final boolean processField;

    private List<JoinOn> joinOnList = new ArrayList<>();

    private JoinOn joinOn;

    public JoinTable(String alias, BeanEntityDef entityDef, SelectBuilder selectBuilder,boolean processField){
        this.alias = alias;
        this.mainEntityDef = entityDef;
//        this.selectConditionBuilder = selectConditionBuilder;
        this.selectBuilder = selectBuilder;
        this.processField = processField;
    }

    public JoinTable leftJoin(String alias,BeanEntityDef joinEntityDef){
        return leftJoinOn(alias,joinEntityDef,null);
    }

    public JoinTable rightJoin(String alias,BeanEntityDef joinEntityDef){
        return rightJoinOn(alias,joinEntityDef,null);
    }

    public JoinTable innerJoin(String alias,BeanEntityDef joinEntityDef){
        return innerJoinOn(alias,joinEntityDef,null);
    }

    public JoinTable leftJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName){
        checkJoin();
        if(processField){
            selectBuilder.bindFields(alias,joinEntityDef.getFieldList());
        }
        join(alias,joinEntityDef,JoinType.LEFT,onAttrName);

        return this;
    }

    public JoinTable rightJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName){
        checkJoin();
        if(processField){
            selectBuilder.bindFields(alias,joinEntityDef.getFieldList());
        }

        join(alias,joinEntityDef,JoinType.RIGHT,onAttrName);
        return this;
    }

    public JoinTable innerJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName){
        checkJoin();
        if(processField){
            selectBuilder.bindFields(alias,joinEntityDef.getFieldList());
        }
        join(alias,joinEntityDef,JoinType.INNER,onAttrName);
        return this;
    }

    public String getAlias(){
        return alias;
    }

    //*****************************************************************

    /**
     * 根据指定的实体进行join条件拼接
     * @param alias 被join的表别名
     * @param joinEntityDef 被join的实体对象
     * @param onAttrName join的属性
     * @param entityDef 根据该实体定义进行关联，该实体对象必须是整个join链中存在的实体
     * @return JoinTable
     */
    public JoinTable leftJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName,BeanEntityDef entityDef){
        joinByEntity(alias,joinEntityDef,JoinType.LEFT,onAttrName,entityDef);
        return this;
    }

    public JoinTable rightJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName,BeanEntityDef entityDef){
        joinByEntity(alias,joinEntityDef,JoinType.RIGHT,onAttrName,entityDef);
        return this;
    }

    public JoinTable innerJoinOn(String alias,BeanEntityDef joinEntityDef,String onAttrName,BeanEntityDef entityDef){
//        checkJoin();
//        if(processField){
//            selectBuilder.bindFields(alias,joinEntityDef.getFieldList());
//        }
//
//        String leftAlias = this.alias;
//        for (JoinOn joinOn : joinOnList) {
//            if(joinOn.leftEntityDef == entityDef){
//                leftAlias = joinOn.leftAlias;
//            }
//        }
//
//        joinOn = new JoinOn(leftAlias,entityDef,alias,joinEntityDef, JoinType.INNER,onAttrName);
//        joinOnList.add(joinOn);
//
//        mainEntityDef = joinEntityDef;
//        this.alias = alias;
        joinByEntity(alias,joinEntityDef,JoinType.INNER,onAttrName,entityDef);
        return this;
    }

    public void joinByEntity(String rightAlias,BeanEntityDef rightEntityDef,JoinType type,String onAttrName,BeanEntityDef leftEntityDef){
        checkJoin();
        if(processField){
            selectBuilder.bindFields(rightAlias,rightEntityDef.getFieldList());
        }

        //根据leftEntityDef参数，从当前join链中寻找匹配的对象作为左表关联对象。
        //leftEntityDef必须是在join链中的实体对象。
        String leftAlias = null;//= this.alias;
        for (JoinOn joinOn : joinOnList) {
            if(joinOn.getLeftEntityDef() == leftEntityDef){
                leftAlias = joinOn.getLeftAlias();
            }
        }

        if(leftAlias == null){
            throw new RuntimeException("拼写" + type + "关联条件错误，指定编码的实体对象不在join链中" + leftEntityDef.getEntityCode());
        }

        joinOn = new JoinOn(leftAlias,leftEntityDef,rightAlias,rightEntityDef, type ,onAttrName);
        joinOnList.add(joinOn);

        mainEntityDef = rightEntityDef;
        this.alias = rightAlias;
    }
    //*****************************************************************

    public JoinTable andOn(String fieldName, ConditionType type,String paramName){
        joinOn.and(fieldName,type,paramName);
        return this;
    }
    public JoinTable orOn(String fieldName, ConditionType type,String paramName){
        joinOn.or(fieldName,type,paramName);
        return this;
    }

    /**
     * Join的规范检查，默认不允许关联超过5张表
     */
    private void checkJoin() {
        if(joinOnList.size() >= 4){
            throw new RuntimeException("【规范】JOIN不能超过5张表");
        }
    }

    private void join(String alias,BeanEntityDef joinEntityDef,JoinType type, String onAttrName) {
        joinOn = new JoinOn(this.alias,mainEntityDef,alias,joinEntityDef, type,onAttrName);
        joinOnList.add(joinOn);

//        joinOnList.add(new JoinOn(this.alias,mainEntityDef,alias,joinEntityDef, type,onAttrName));
        mainEntityDef = joinEntityDef;
        this.alias = alias;
    }

    BeanEntityDef getMainEntityDef() {
        return mainEntityDef;
    }

    List<JoinOn> getJoinOnList() {
        return Collections.unmodifiableList(joinOnList);
    }

    BeanEntityDef getJoinEntityDef(String alias){
        if(joinOnList.isEmpty() && this.alias.equalsIgnoreCase(alias)){
            return mainEntityDef;
        }
        for (JoinOn joinOn : joinOnList) {
            if(joinOn.getRightAlias().equalsIgnoreCase(alias)){
                return joinOn.getRightEntityDef();
            }else if(joinOn.getLeftAlias().equalsIgnoreCase(alias)){
                return joinOn.getLeftEntityDef();
            }
        }
        return null;
    }

    /**
     * 两表进行关联时的join关联对象，通过{@link #getJoinCondition(Map)}方法得到当前Join的SQL语句片段。<p/>
     * 默认如果对象存在主外键约束关联，框架可以自动识别join的on条件，但如果没有建立主外键关联，则需要手动通过
     * {@link JoinTable#andOn(String, ConditionType, String)}或{@link JoinTable#orOn(String, ConditionType, String)}
     * 指定关联属性,如果主子表关联属性名不一致，则需要以冒号分隔属性名
     * 的方式提供关联条件，例如："primaryId:foreignId"，此时注意主表属性在左侧，子表属性在右侧。
     * @author LiuHG
     */
    public static class JoinOn{
        private final String leftAlias;
        private final BeanEntityDef leftEntityDef;
        private final String rightAlias;
        private final BeanEntityDef rightEntityDef;
        private final JoinType type;
        private final String onAttrName;

        private ConditionBuilder conditionBuilder;

        public JoinOn(String leftAlias,BeanEntityDef leftEntityDef,String rightAlias,BeanEntityDef rightEntityDef,JoinType type, String onAttrName){
            this.leftAlias = leftAlias;
            this.leftEntityDef = leftEntityDef;
            this.rightAlias = rightAlias;
            this.rightEntityDef = rightEntityDef;
            this.type = type;
            this.onAttrName = onAttrName;
        }

        public void and(String fieldName, ConditionType type,String paramName){
            getConditionBuilder().and(fieldName,type,paramName);
        }

        public void or(String fieldName, ConditionType type,String paramName){
            getConditionBuilder().or(fieldName,type,paramName);
        }

        private ConditionBuilder getConditionBuilder(){
            if(conditionBuilder == null){
                conditionBuilder = new ConditionBuilder(){
                    @Override
                    protected boolean checkRequired(String attrName) {
                        return true;
                    }
                };
            }
            return conditionBuilder;
        }

        /**
         * 返回join的属性名，固定返回长度为2的数组
         * @return
         */
        public String[] getJoinAttrName() {
            if(onAttrName != null){
                String[] joinSplit = onAttrName.split(":");
                if(joinSplit.length == 1){
                    return new String[]{onAttrName,onAttrName};
                }
                return joinSplit;
            }
            BeanEntityDef[] leftFkFieldList = leftEntityDef.getFkBeanEntityDef();
            BeanEntityDef[] rightFkFieldList = rightEntityDef.getFkBeanEntityDef();

            BeanFieldDef leftPkFieldDef = leftEntityDef.getPkFieldDef();
            if(leftFkFieldList == null || leftFkFieldList.length == 0){
                String attrName = leftPkFieldDef.getAttrName();
                return new String[]{attrName,attrName};
            }

            BeanFieldDef rightPkFieldDef = rightEntityDef.getPkFieldDef();
            if(rightFkFieldList == null || rightFkFieldList.length == 0){
                String attrName = rightPkFieldDef.getAttrName();
                return new String[]{attrName,attrName};
            }

            for (BeanEntityDef beanEntityDef : rightFkFieldList) {
                if(beanEntityDef == leftEntityDef){
                    String attrName = leftPkFieldDef.getAttrName();
                    return new String[]{attrName,attrName};
                }
            }

            String attrName = rightPkFieldDef.getAttrName();
            return new String[]{attrName,attrName};
        }

        public String getLeftTable() {
            SqlStringSplicer sqlplicer = new SqlStringSplicer();
            sqlplicer.appendWrapped(leftEntityDef.getTableName());
            sqlplicer.appendSpace();
            sqlplicer.appendWrapped(leftAlias);
            return sqlplicer.toString();
        }

        public String getRightTable() {
            SqlStringSplicer sqlplicer = new SqlStringSplicer();
            sqlplicer.appendWrapped(rightEntityDef.getTableName());
            sqlplicer.appendSpace();
            sqlplicer.appendWrapped(rightAlias);
            return sqlplicer.toString();
        }

        public BeanEntityDef getLeftEntityDef() {
            return leftEntityDef;
        }

        public BeanEntityDef getRightEntityDef() {
            return rightEntityDef;
        }

        public String getLeftAlias() {
            return leftAlias;
        }

        public String getRightAlias() {
            return rightAlias;
        }

        public String getJoinCondition(Map<String, Object> paramMap) {
//            if(leftEntityDef == rightEntityDef){
//                throw new RuntimeException("【规范】不允许JOIN自己");
//            }

//            String conditionSql;
            SqlStringSplicer sqlBuidler = new SqlStringSplicer();

            if(onAttrName != null){
                String[] joinSplit = onAttrName.split(":");//判断是否按照不同字端进行关联
                String leftJoinAttrName = onAttrName;
                String rightJoinAttrName = onAttrName;
                if(joinSplit.length > 1){
                    leftJoinAttrName = joinSplit[0];
                    rightJoinAttrName = joinSplit[1];
                }else{
                    leftJoinAttrName = onAttrName;
                    rightJoinAttrName = onAttrName;
                }
                BeanFieldDef leftFieldDef = leftEntityDef.getFieldDef(leftJoinAttrName);

                BeanFieldDef rightFieldDef = rightEntityDef.getFieldDef(rightJoinAttrName);
                if(leftFieldDef == null || rightFieldDef == null){
                    throw new RuntimeException("关联查询的ON属性需要同时存在于两个（" + leftEntityDef.getTableName() +
                            "表中对应" + leftJoinAttrName + "属性和" + rightEntityDef.getTableName()
                            +"表中对应" +rightJoinAttrName + "属性）表对象中，同时请注意需要提供的是属性名，不是表字段名。");
                }
//                conditionSql = leftAlias + "." + leftFieldDef.getFieldName() + "=" + rightAlias + "." + rightFieldDef.getFieldName();
                sqlBuidler.appendWrapped(leftAlias,leftFieldDef.getFieldName());
                sqlBuidler.append('=');
                sqlBuidler.appendWrapped(rightAlias,rightFieldDef.getFieldName());
            }else{
                String conditionSql = getConditionSql(leftEntityDef,rightEntityDef);
                if(conditionSql == null){
                    conditionSql = getConditionSql(rightEntityDef,leftEntityDef);
                }
                if(conditionSql == null){
                    throw new RuntimeException("【规范】表之间没有主外键关系，无法建立JOIN关联");
                }
                sqlBuidler.append(conditionSql);
            }

            if(conditionBuilder != null){
//                conditionSql += conditionBuilder.toCondition(paramMap,false);
                sqlBuidler.append(conditionBuilder.toCondition(paramMap,false));
            }
            return sqlBuidler.toString();
//            return conditionSql;
        }

        private String getConditionSql(BeanEntityDef def1,BeanEntityDef def2){
            BeanEntityDef[] fkFieldDefList = def2.getFkBeanEntityDef();

//            BeanEntityDef[] fkFieldDef = leftEntityDef.getFkFieldDef();

            BeanFieldDef pkDef = def1.getPkFieldDef();
            String fieldName = pkDef.getFieldName();

            if(fkFieldDefList != null){
                for(BeanEntityDef def : fkFieldDefList){
                    if(def == def1){
                        BeanFieldDef fkDef = def.getPkFieldDef();
//                        return leftAlias + "." + fieldName + "=" + rightAlias + "." + fkDef.getFieldName();
                        SqlStringSplicer sqlBuidler = new SqlStringSplicer();
                        sqlBuidler.appendWrapped(leftAlias,fieldName);
                        sqlBuidler.append('=');
                        sqlBuidler.appendWrapped(rightAlias,fkDef.getFieldName());
                        return sqlBuidler.toString();
                    }
                }
            }
            return null;
        }


        public JoinType getType() {
            return type;
        }
    }

    public SelectConditionBuilder where(){
        return selectBuilder.where(true);
    }

    public enum JoinType{
        LEFT("LEFT JOIN"),RIGHT("RIGHT JOIN"),INNER("INNER JOIN");

        private final String joinSql;

        JoinType(String joinSql){
            this.joinSql = joinSql;
        }

        public String getJoinSql() {
            return joinSql;
        }
    }
}
