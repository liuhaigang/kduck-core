package cn.kduck.core.dao.sqlbuilder;

import cn.kduck.core.service.ParamMap;
import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.query.CustomQueryBean;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import cn.kduck.core.dao.sqlbuilder.SelectBuilder.AggregateType;
import cn.kduck.core.utils.BeanDefUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.*;

/**
 * @author LiuHG
 */
@TestMethodOrder(MethodName.class)
public class SelectBuilderTest {

    private ObjectMapper jsonMapper = new ObjectMapper();

    private BeanEntityDef userEntityDef;
    private BeanEntityDef orgEntityDef;
    private BeanEntityDef orgUserEntityDef;

    @BeforeEach
    public void setUp() throws Exception {

//      USER
        BeanFieldDef userIdFieldDef = new BeanFieldDef("userId","USER_ID",String.class,true);
        BeanFieldDef userNameFieldDef = new BeanFieldDef("userName","USER_NAME",String.class);
        BeanFieldDef userGenderFieldDef = new BeanFieldDef("gender","GENDER",Integer.class);
        BeanFieldDef userBirthdayFieldDef = new BeanFieldDef("birthday","BIRTHDAY",Date.class);
        BeanFieldDef ageBirthdayFieldDef = new BeanFieldDef("age","AGE",Integer.class);
        BeanFieldDef enableFieldDef = new BeanFieldDef("enable","ENABLE",Integer.class);

        BeanFieldDef[] userFieldDefs = new BeanFieldDef[]{userIdFieldDef,userNameFieldDef,userGenderFieldDef,userBirthdayFieldDef,ageBirthdayFieldDef,enableFieldDef};
        userEntityDef = new BeanEntityDef("DEMO_USER",Arrays.asList(userFieldDefs));

        //*******************************************************************************************
//      ORG
        BeanFieldDef orgIdFieldDef = new BeanFieldDef("orgId","ORG_ID",String.class,true);
        BeanFieldDef orgNameFieldDef = new BeanFieldDef("orgName","ORG_NAME",String.class);
        BeanFieldDef orgCodeFieldDef = new BeanFieldDef("orgCode","ORG_CODE",String.class);

        BeanFieldDef[] orgFieldDefs = new BeanFieldDef[]{orgIdFieldDef,orgNameFieldDef,orgCodeFieldDef};
        orgEntityDef = new BeanEntityDef("DEMO_ORG",Arrays.asList(orgFieldDefs));

        //*******************************************************************************************
//      ORG_USER
        BeanFieldDef orgUserIdFieldDef = new BeanFieldDef("orgUserId","ORG_USER_ID",String.class,true);
        BeanFieldDef[] orgUserFieldDefs = new BeanFieldDef[]{orgUserIdFieldDef,userIdFieldDef,orgIdFieldDef};
        orgUserEntityDef = new BeanEntityDef("DEMO_ORG_USER",Arrays.asList(orgUserFieldDefs));
        orgUserEntityDef.setFkBeanEntityDef(new BeanEntityDef[]{userEntityDef,orgEntityDef});

        //设置了一个全局条件
//        new QueryConditionContext(Arrays.asList(new UserConditionDefiner()));
    }

    private enum GenderType {
        MALE,
        FEMALE
    }

    @Test
    public void t000_Update_Delete() {

//        Map<String, Object> paramMap = ParamMap.create("userName", "赵").set("enable",0).set("userAge",65).toMap();
//        UpdateBuilder updateBuiler = new UpdateBuilder(userEntityDef, paramMap);
//        updateBuiler.where().and("USER_NAME", ConditionType.CONTAINS,"userName").and("USER_AGE",ConditionType.GREATER,"userAge");
//        printSql("条件更新",updateBuiler.build());

        Map<String, Object> paramMap = ParamMap.create("userName", "刚").set("gender",GenderType.MALE).set("userAge",13).toMap();

        List<Map<String,Object>> paramMapList = new ArrayList<>();
        paramMapList.add(paramMap);
        InsertBuilder insterBulider = new InsertBuilder(userEntityDef,paramMapList);
        printSql("添加数据",insterBulider.build());

        UpdateBuilder updateBuiler = new UpdateBuilder(userEntityDef, paramMap);
        updateBuiler.where().and("USER_NAME", ConditionType.CONTAINS,"userName").or("USER_AGE",ConditionType.LESS,"userAge");
        System.out.println(updateBuiler.build().getSql());
        System.out.println(updateBuiler.build().getSql());
        printSql("条件更新",updateBuiler.build());

        paramMap = ParamMap.create("userName", "刚").set("gender",GenderType.MALE).set("userAge",13).toMap();
        DeleteBuilder deleteBuiler = new DeleteBuilder(userEntityDef, paramMap);
        deleteBuiler.where().and("USER_NAME", ConditionType.CONTAINS,"userName").or("USER_AGE",ConditionType.LESS,"userAge");
        System.out.println(deleteBuiler.build().getSql());
        System.out.println(deleteBuiler.build().getSql());
        printSql("条件删除",deleteBuiler.build());
    }


    @Test
    public void t001_1Table() {
        SelectBuilder sqlBuiler = new SelectBuilder(userEntityDef);
        printSql("单表查询-1",sqlBuiler);

        Map<String, Object> paramMap = ParamMap.create("userName", "刚").toMap();

        sqlBuiler = new SelectBuilder(userEntityDef, paramMap);
        sqlBuiler.where("USER_NAME", ConditionType.CONTAINS,"userName");
        sqlBuiler.bindAliasField("", BeanDefUtils.getByAttrName(userEntityDef.getFieldList(),"gender"),"aaa");
        printSql("单表查询-2",sqlBuiler);

        sqlBuiler = new SelectBuilder(userEntityDef,paramMap);
        sqlBuiler.bindFields("",BeanDefUtils.includeField(userEntityDef.getFieldList(),"userId"));
        sqlBuiler.bindAggregate("USER_ID",AggregateType.COUNT);
        sqlBuiler.where("USER_NAME", ConditionType.CONTAINS,"userName")
                .and("USER_NAME",ConditionType.NOT_IN,"userName")
                .and("USER_NAME",ConditionType.IS_EMPTY);
        sqlBuiler.bindAliasField("",BeanDefUtils.getByAttrName(userEntityDef.getFieldList(),"gender"),"sex");
        sqlBuiler.bindAlias("USER_ID","uid");
        printSql("单表查询-3",sqlBuiler);

        sqlBuiler = new SelectBuilder(userEntityDef, paramMap);
        sqlBuiler.from("u",userEntityDef).where().and("u.USER_NAME", ConditionType.CONTAINS,"userName");
        printSql("单表查询-4",sqlBuiler);

        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.from("u",userEntityDef).innerJoinOn("u2",userEntityDef,"userId").where().and("u.USER_NAME", ConditionType.CONTAINS,"userName");
        printSql("单表查询-5",sqlBuiler);

        sqlBuiler = new SelectBuilder(userEntityDef,paramMap);
        sqlBuiler.bindFields("",BeanDefUtils.includeField(userEntityDef.getFieldList(),"userId","userName"));
        sqlBuiler.bindAggregate("USER_NAME",AggregateType.COUNT);
        sqlBuiler.bindAlias("USER_ID","uid");
        sqlBuiler.bindAlias("USER_NAME","uname");
        printSql("单表查询-6",sqlBuiler);
    }

    @Test
    public void t002_2Tables() {
        Map<String, Object> paramMap = ParamMap.create("userName", "刚").set("age","12").toMap();

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
//        sqlBuiler.bindFields("a").bindFields(true,"b","orgUserId");
//        sqlBuiler.bindAlias("a.USER_ID","id");
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
        .where().and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName").or("a.AGE", ConditionType.IS_NOT_EMPTY);
        sqlBuiler.bindAggregate("a.AGE", AggregateType.COUNT);
        printSql("两表查询",sqlBuiler);
    }

    @Test
    public void t003_3Tables() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userName","某某某");
        paramMap.put("gender","1");

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList());
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef).leftJoin("c",orgEntityDef).where().
                and("a.USER_NAME", ConditionType.CONTAINS,"userName")
                .and("a.GENDER", ConditionType.EQUALS,"gender");
        printSql("三表查询",sqlBuiler);

        paramMap.put("orgId","orgIdValue");
        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList());
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef).andOn("b.ORG_ID",ConditionType.EQUALS,"orgId").leftJoin("c",orgEntityDef).where().and("a.USER_NAME", ConditionType.CONTAINS,"userName");
        printSql("三表查询（多join条件）",sqlBuiler);

        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList());
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef).innerJoinOn("c",orgEntityDef,"userId:orgId",userEntityDef).where().and("a.USER_NAME", ConditionType.CONTAINS,"userName");
        printSql("三表查询（自定义join的实体）",sqlBuiler);
    }

    @Test
    public void t004_StaticValue() {
        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("userName","某某某");
        paramMap.put("date",new Date());

        SelectBuilder sqlBuiler = new SelectBuilder(userEntityDef,paramMap);
        sqlBuiler.
                where("USER_NAME", ConditionType.CONTAINS,"userName").
                and("GENDER",ConditionType.EQUALS,"${1}").
                or("BIRTHDAY",ConditionType.LESS,"date").
                orderBy().
                asc("USER_NAME");
        printSql("条件查询",sqlBuiler);
    }

    @Test
    public void t005_GroupByCondition() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userName","某某某");

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.bindAggregate("a.USER_NAME",AggregateType.COUNT);
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
        .where().and("a.USER_NAME", ConditionType.CONTAINS,"userName").groupBy("a.GENDER","a.USER_NAME").orderBy().asc("a.USER_NAME");

        printSql("分组查询查询",sqlBuiler);
    }

    @Test
    public void t006_SplitGroupCondition() {
        Map<String, Object> paramMap = ParamMap.create("userName", "刚").set("age","18").set("orgId","机构Id").set("gender","1").toMap();

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName")
                .groupBegin("a.BIRTHDAY",ConditionType.LESS,"age").and("b.ORG_ID",ConditionType.EQUALS,"orgId");
        printSql("组条件查询（尾）",sqlBuiler);

        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.GENDER", ConditionType.EQUALS,"gender")
                .groupBegin("a.BIRTHDAY",ConditionType.LESS,"age").and("b.ORG_ID",ConditionType.EQUALS,"orgId")
                .groupEnd().and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName");
        printSql("组条件查询（中）",sqlBuiler);

        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where()
                .groupBegin("a.ORG_ID",ConditionType.LESS,"orgId1")
                .or("b.ORG_ID",ConditionType.EQUALS,"orgId")
                .or("b.ORG_ID",ConditionType.EQUALS,"${'1'}")
                .groupEnd().and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName");
        printSql("组条件查询（首）",sqlBuiler);

        paramMap.put("a","valueA");
        paramMap.put("b","valueB");
        paramMap.put("c","valueC");
        paramMap.put("d","valueD");
        sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where()
                .groupBegin("a.ORG_ID",ConditionType.LESS,"orgId1")
                .or("b.ORG_ID",ConditionType.EQUALS,"orgId")
                .or("b.ORG_ID",ConditionType.EQUALS,"${'1'}")
                .groupBegin("a.VALUE_A",ConditionType.LESS,"a")
                .or("b.VALUE_C",ConditionType.EQUALS,"noValue")
                .or("b.VALUE_B",ConditionType.EQUALS,"b")
                .groupEnd()
                .groupEnd().and("a.USER_NAME", ConditionType.BEGIN_WITH,"userName");
        printSql("组条件查询（嵌套）",sqlBuiler);

    }

    @Test
    public void t007_InCondition() {
        int userNum = 10;
        String[] userIds = new String[userNum];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = "userid_value_" + i;
        }
        Map<String, Object> paramMap = ParamMap.create("birthday", new Date()).set("userIds",userIds).toMap();

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.BIRTHDAY", ConditionType.LESS,"birthday")
                .and("a.USER_ID",ConditionType.IN,"userIds");
        printSql("IN条件查询",sqlBuiler);
    }

    @Test
    public void t008_DynamicOrderBy() {
        Map<String, Object> paramMap = ParamMap.create("birthday", new Date()).set("userNameSort","Desc").set("genderSort","asc").toMap();

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.BIRTHDAY", ConditionType.LESS,"birthday")
                .and("a.USER_ID",ConditionType.IN,"userIds")
                .orderByDynamic().mapping("a.USER_NAME","userNameSort").mapping("a.GENDER","genderSort");
        printSql("动态排序",sqlBuiler);
    }

    @Test
    public void t009_Subquery() {

//        //构建子查询
//        Map<String, Object> subqueryParamMap = ParamMap.create("gender","1").toMap();
//        SelectBuilder subqueryBuiler = new SelectBuilder(userEntityDef, subqueryParamMap);
//        subqueryBuiler.bindFields("",BeanDefUtils.includeField(userEntityDef.getFieldList(),"userId"));
//        subqueryBuiler.where("GENDER", ConditionType.EQUALS,"gender");
//
//        //构建主查询
//        Map<String, Object> paramMap = ParamMap.create("userName","刘").toMap();
//        SelectBuilder sqlBuiler = new SelectBuilder(userEntityDef, paramMap);
//        sqlBuiler.where("USER_NAME", ConditionType.CONTAINS,"userName")
//            .or("USER_ID",ConditionType.IN,subqueryBuiler.build())
//            .orderBy().asc("USER_NAME");
//        printSql("子查询",sqlBuiler.build());

        Map<String, Object> paramMap = ParamMap.create("userName", "刚")
                .set("userId", "317")
                .set("age","18")
                .set("birthday","1982-3-17")
                .set("orgId","机构Id")
                .set("gender","1").toMap();

        SelectBuilder subqueryBuiler = new SelectBuilder(userEntityDef, paramMap);
        subqueryBuiler.bindFields("",BeanDefUtils.includeField(userEntityDef.getFieldList(),"userId"));
        subqueryBuiler.where("USER_ID", ConditionType.EQUALS,"userId");


        SelectBuilder subquery2Builer = new SelectBuilder(paramMap);
        subquery2Builer.bindFields("a",BeanDefUtils.includeField(userEntityDef.getFieldList(),"birthday"))
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        subquery2Builer.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.USER_NAME", ConditionType.CONTAINS,"userName")
                .groupBegin("a.BIRTHDAY",ConditionType.LESS,"birthday").and("b.ORG_ID",ConditionType.EQUALS,"orgId");


        SelectBuilder sqlBuiler = new SelectBuilder(userEntityDef, paramMap);
        sqlBuiler.where()
            .and("USER_ID",ConditionType.EQUALS,subqueryBuiler.build())
            .and("GENDER",ConditionType.EQUALS,"gender")
            .and("AGE",ConditionType.GREATER_OR_EQUALS,"age")
            .or("BIRTHDAY",ConditionType.IN,subquery2Builer.build()).orderBy().asc("USER_NAME");
        sqlBuiler.bindAliasField("",BeanDefUtils.getByAttrName(userEntityDef.getFieldList(),"gender"),"aaa");
//        sqlBuiler.bindFields("","gender:lhg");
        printSql("子查询",sqlBuiler.build());
    }

    @Test
    public void t010_GroupByHavingCondition() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userName","某某某");
        paramMap.put("gender",1);

        SelectBuilder sqlBuiler = new SelectBuilder(paramMap);
        sqlBuiler.bindFields("a",userEntityDef.getFieldList())
                .bindFields("b", BeanDefUtils.excludeField(orgUserEntityDef.getFieldList(),"userId"));
        sqlBuiler.bindAggregate("a.USER_NAME",AggregateType.COUNT);
        sqlBuiler.from("a",userEntityDef).innerJoin("b",orgUserEntityDef)
                .where().and("a.USER_NAME", ConditionType.CONTAINS,"userName").groupBy("a.GENDER","a.USER_NAME")
                .having().and("a.GENDER",ConditionType.EQUALS,"gender").orderBy().asc("a.USER_NAME");

        printSql("分组查询查询",sqlBuiler);
    }

    @Test
    public void t100_Partial_Extend() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userName","某某某");
        paramMap.put("date",new Date());

        SelectBuilder sqlBuiler = new SelectBuilder("SELECT {*} FROM DEMO_USER",paramMap,userEntityDef.getFieldList());
        sqlBuiler.
                where("USER_NAME", ConditionType.CONTAINS,"userName").
                and("GENDER",ConditionType.EQUALS,"${1}").
                or("BIRTHDAY",ConditionType.LESS,"date").
                orderBy().
                asc("USER_NAME");
        printSql("扩展查询",sqlBuiler);
    }

    @Test
    public void t200_Full_Extend() {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userName","%某某某%");
        paramMap.put("date",new Date());

        CustomQueryBean customQueryBean = new CustomQueryBean("SELECT * FROM DEMO_USER WHERE USER_NAME LIKE #{userName}",paramMap);
        printSql("自定义查询",customQueryBean);
    }

    private void printSql(String title,SelectBuilder sqlBuiler){
        QuerySupport querySupport = sqlBuiler.build();
        printSql(title,querySupport);
    }

    private void printSql(String title,QuerySupport querySupport){
        SqlObject sqlObject = querySupport.getQuery(null);
        printSql(title,sqlObject);
    }

    private void printSql(String title,SqlObject sqlObject){
        try {
            System.out.println(title + "\r\nSQL: " + sqlObject.getSql() + "\r\n" +
                    "Params: "+jsonMapper.writeValueAsString(sqlObject.getParamValueList()) + "\r\n" +
                    "Fields: " + sqlObject.getFieldDefList().size() + "\r\n");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}