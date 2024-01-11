package cn.kduck.core.service;

import cn.kduck.core.dao.DeleteArchiveHandler;
import cn.kduck.core.dao.FieldFilter;
import cn.kduck.core.dao.JdbcEntityDao;
import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.id.IdGenerator;
import cn.kduck.core.dao.id.impl.KduckIdGenerator;
import cn.kduck.core.dao.id.impl.SnowFlakeGenerator;
import cn.kduck.core.dao.id.impl.UuidGenerator;
import cn.kduck.core.dao.query.CustomQueryBean;
import cn.kduck.core.dao.query.QueryCreator;
import cn.kduck.core.dao.query.QueryFactory;
import cn.kduck.core.dao.query.QuerySupport;
import cn.kduck.core.dao.sqlbuilder.ConditionBuilder.ConditionType;
import cn.kduck.core.dao.sqlbuilder.DeleteBuilder;
import cn.kduck.core.dao.sqlbuilder.InsertBuilder;
import cn.kduck.core.dao.sqlbuilder.SelectBuilder;
import cn.kduck.core.dao.sqlbuilder.UpdateBuilder;
import cn.kduck.core.dao.sqlbuilder.template.update.UpdateFragmentTemplate;
import cn.kduck.core.service.autofill.AutofillValue;
import cn.kduck.core.service.autofill.AutofillValue.FillType;
import cn.kduck.core.service.autofill.impl.StandardFieldAutofill;
import cn.kduck.core.service.exception.QueryNotFoundException;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OidHolder;
import cn.kduck.core.web.interceptor.OperateIdentificationInterceptor.OperateIdentification;
import cn.kduck.core.web.interceptor.operateinfo.OperateObject;
import cn.kduck.core.web.interceptor.operateinfo.OperateObject.OperateType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 默认的Service实现类，提供一套常用的增删改查方法，内部会调用框架的数据访问对象进行数据操作。<p/>
 * 该类可独立以注入的方式使用，也常用于被子类集成完成更加灵活复杂的业务场景。
 * 要求在开发时，必须使用本类提供的方法完成业务逻辑。
 *
 * @author LiuHG
 */
@Service
public class DefaultService implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    private static final UpdateFragmentTemplate[] EMPTY_TEMPLATE = new UpdateFragmentTemplate[0];

    @Autowired
    private BeanDefDepository beanDefDepository;

    @Autowired
    private JdbcEntityDao jdbcEntityDao;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private QueryFactory queryFactory;

    private boolean strict = false;

    @Autowired(required = false)
    private AutofillValue autofillValue;

    /**
     * 在添加和修改前，对数据提供一次预前处理的机会。删除和查询操作时不受该方法的控制
     * @param valueBean 预添加和修改的数据值对象
     */
    protected void fieldFilter(ValueBean valueBean){}

    /**
     * 根据当前的主键生成策略为数据值对象(ValueBean)生成主键值，并赋予到valueBean的主键属性上。
     * 如果仅希望得到一个主键值，可通过注入{@link IdGenerator}对象来获取。
     * @param valueBean 数据值对象
     * @return 返回为数据值对象生成的主键值
     * @see SnowFlakeGenerator SnowFlakeGenerator
     * @see UuidGenerator UuidGenerator
     * @deprecated 使用generateIdValue()替换
     */
    public Serializable generateIdValue(ValueBean valueBean){
        Serializable idValue = idGenerator.nextId();
        valueBean.setIdValue(idValue);
        return idValue;
    }

    /**
     * 生成一个主键值
     * @return 用于主键的唯一值
     * @see IdGenerator
     */
    public Serializable generateIdValue(){
        return idGenerator.nextId();
    }

    /*
     * 处理js不支持长long，将long转换为字符串供页面输出
     */
    private Object processIdtoString(Serializable idValue){
        if(idValue.getClass() == Long.TYPE || idValue.getClass() == Long.class){
            return idValue.toString();
        }else{
            return idValue;
        }
    }

    /**
     * 数据的添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。批量操作请参考{@link #batchAdd(ValueBean[])}
     * @param valueBean 数据值对象
     * @return 数据的主键值
     * @see #add(ValueBean, boolean)
     * @deprecated 使用add(String entityDefName,Map valueMap)替换
     */
    public Serializable add(ValueBean valueBean){
        return add(valueBean,true);
    }

    /**
     * 数据的添加方法。由调用者决定是否需要重置主键值。批量操作请参考{@link #batchAdd(ValueBean[], boolean)}
     * @param valueBean 数据值对象
     * @param resetPk 是否需要重置主键值（赋予新值）。
     *                true 重置主键值，功能等同于{@link #add(ValueBean)}；
     *                false 不重置主键值，即主键值在开发时提供，valueBean中须含有主键值
     * @return 数据的主键值
     * @deprecated 使用add(String entityDefName,Map valueMap,boolean resetPk)替换
     */
    public Serializable add(ValueBean valueBean,boolean resetPk){
        Serializable[] serializables = batchAdd(new ValueBean[]{valueBean}, resetPk);
        return serializables[0];
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。单对象保存请参考{@link #add(ValueBean)}
     * @param valueBeans 数据值对象数组
     * @return 数据的主键值
     * @deprecated 使用batchAdd(String entityDefName, Map[] valueMaps)替换
     */
    public Serializable[] batchAdd(ValueBean[] valueBeans){
        return batchAdd(valueBeans,true);
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。单对象保存请参考{@link #add(ValueBean)}
     * @param valueBeans 数据值对象数组
     * @param resetPk 是否需要重置主键值（赋予新值）。
     *                true 重置主键值，功能等同于{@link #batchAdd(ValueBean[])}；
     *                false 不重置主键值，即主键值在开发时提供，valueBean中须含有主键值
     * @return 数据的主键值
     * @deprecated 使用batchAdd(String entityDefName, Map[] valueMaps, boolean resetPk)替换
     */
    public Serializable[] batchAdd(ValueBean[] valueBeans, boolean resetPk){
        Serializable[] idValues = new Serializable[valueBeans.length];

        int idCount = 0;
        for (int i = 0; i < valueBeans.length; i++) {
            if(resetPk){
                generateIdValue(valueBeans[i]);
            }
            idValues[i] = valueBeans[i].getIdValue();
            fieldFilter(valueBeans[i]);
            processFillValue(FillType.ADD,valueBeans[i]);

            if(idValues[i] != null){
                idCount++;
            }
        }

        //如果主键值有为null的情况，则猜测数据库的主键字段为自增长，并在添加后获取到自增主键值赋值到实体中。
        boolean guessGeneratedKeys = valueBeans.length > idCount;

        Assert.notEmpty(valueBeans,"保存的数据对象不能为空");
        BeanEntityDef entityDef = valueBeans[0].getEntityDef();

        List<Map<String,Object>> paramMapList = new ArrayList<>(valueBeans.length);
        for(ValueBean valueBean : valueBeans){
            paramMapList.add(valueBean.getValueMap());

            addOperateObject(OperateType.INSERT,valueBean);
        }

        InsertBuilder insertBuilder = new InsertBuilder(entityDef,paramMapList);
        SqlObject sqlObject = insertBuilder.build();

        if(guessGeneratedKeys){
            List<Object> keyHolder = new ArrayList<>();
            jdbcEntityDao.execute(sqlObject,keyHolder);
            for (int i = 0; i < keyHolder.size(); i++) {
                valueBeans[i].setIdValue(keyHolder.get(i));
                idValues[i] = (Serializable) keyHolder.get(i);
            }
        }else{
            jdbcEntityDao.execute(sqlObject);
        }

        //处理js不支持长long的情况
        for (ValueBean valueBean : valueBeans) {
            Serializable idValue = valueBean.getIdValue();
            valueBean.setIdValue(processIdtoString(idValue));
        }

        return idValues;
    }

    protected void addOperateObject(OperateType type,ValueBean valueBean){
        OperateIdentification operateIdentification = OidHolder.getOperateIdentification();

        if(operateIdentification == null) {
            // 未经过审核过滤器，无法将操作对象记录进审计信息中
            return;
        }

        BeanEntityDef entityDef = valueBean.getEntityDef();

        if(OperateType.INSERT == type && "K_DELETE_ARCHIVE".equalsIgnoreCase(entityDef.getTableName())) {
            return;
        }

        Map<String, Object> valueMap = valueBean.getValueMap();
        operateIdentification.addOperateObject(new OperateObject(type,entityDef,Collections.unmodifiableMap(valueMap)));
    }

    /**
     * 数据的添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。
     * @param entityDefName 实体定义编码名
     * @param valueMap 数据值
     * @return 数据的主键值
     */
    public Serializable add(String entityDefName,Map valueMap){
        return add(entityDefName,valueMap,true);
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。
     * @param entityDefName 实体定义编码名
     * @param valueMaps 数据值
     * @return 数据的主键值
     */
    public Serializable[] batchAdd(String entityDefName, Map[] valueMaps){
        return batchAdd(entityDefName,valueMaps,null);
    }

    public Serializable[] batchAdd(String entityDefName, Map[] valueMaps,boolean resetPk){
        ValueBean[] valueBeans = convertValueBeans(entityDefName, valueMaps, null);
        return batchAdd(valueBeans,resetPk);
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。
     * @param entityDefName 实体定义编码名
     * @param valueMaps 数据值
     * @param extParams 扩展属性，用于为数组中每个元素都加入提供的扩展属性
     * @return 数据的主键值
     */
    public Serializable[] batchAdd(String entityDefName, Map[] valueMaps, Map<String,Object> extParams){
        return batchAdd(entityDefName,valueMaps,extParams,true);
    }

    public Serializable[] batchAdd(String entityDefName, Map[] valueMaps, Map<String,Object> extParams,boolean resetPk){
        ValueBean[] valueBeans = convertValueBeans(entityDefName, valueMaps, extParams);
        return batchAdd(valueBeans,resetPk);
    }

    private ValueBean[] convertValueBeans(String entityDefName, Map[] valueMaps, Map<String, Object> extParams) {
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean[] valueBeans = new ValueBean[valueMaps.length];
        for (int i = 0; i < valueMaps.length; i++) {

            if(extParams != null && !extParams.isEmpty()){
                valueMaps[i].putAll(extParams);
            }
            valueBeans[i] = new ValueBean(beanEntityDef,valueMaps[i],false);
        }
        return valueBeans;
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。
     * @param entityDefName 实体定义编码名
     * @param valueMaps 数据值
     * @return 数据的主键值
     */
    public Serializable[] batchAdd(String entityDefName, List<? extends Map> valueMaps){
        return batchAdd(entityDefName,valueMaps,null);
    }

    /**
     * 数据的批量添加方法。负责主键值的自动生成，手动设置的主键值会被覆盖。
     * @param entityDefName 实体定义编码名
     * @param valueMaps 数据值
     * @param extParams 扩展属性，用于为List每个元素都加入提供的扩展属性
     * @return 数据的主键值
     */
    public Serializable[] batchAdd(String entityDefName, List<? extends Map> valueMaps, Map<String,Object> extParams){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean[] valueBeans = new ValueBean[valueMaps.size()];
        for (int i = 0; i < valueBeans.length; i++) {
            Map map = valueMaps.get(i);
            ValueMap valueMap;
            if(map instanceof ValueMap){
                valueMap = (ValueMap) map;
            }else{
                valueMap = new ValueMap(map);
            }

            if(extParams != null && !extParams.isEmpty()){
                valueMap.putAll(extParams);
            }

            valueBeans[i] = new ValueBean(beanEntityDef,valueMap,false);
        }
        return batchAdd(valueBeans,true);
    }

    /**
     * 数据的添加方法。由调用者决定是否需要重置主键值。
     * @param entityDefName 实体定义编码名
     * @param valueMap 数据值
     * @param resetPk 是否需要重置主键值（赋予新值）。
     *                true 重置主键值，功能等同于{@link #add(String, Map)}；
     *                false 不重置主键值，即主键值在开发时提供，valueBean中须含有主键值
     * @return 数据的主键值
     */
    public Serializable add(String entityDefName,Map valueMap,boolean resetPk){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean valueBean = new ValueBean(beanEntityDef, valueMap,strict);
        return add(valueBean,resetPk);
    }


    /**
     * 根据提供的主键，批量删除指定的数据。<p/>
     * 框架在删除时采用物理删除，但默认会将被删除的数据归档到K_DELETE_ARCHIVE表（需提前创建），
     * 也可以根据需要自行处理被删数据的归档方式，如果需要自定义，需要实现{@link DeleteArchiveHandler DeleteArchiveHandler}
     * 接口并声明为Spring的Bean，注：不能以@Bean的方式声明，否则无效。
     * @param entityDefName 实体定义编码名
     * @param ids 数据主键数组
     */
    public void delete(String entityDefName,Serializable[] ids){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        BeanFieldDef pkFieldDef = beanEntityDef.getPkFieldDef();

        delete(ids, beanEntityDef, pkFieldDef);
    }

    /**
     * 根据其他键值，删除指定的数据。<p/>
     * 框架在删除时采用物理删除，但默认会将被删除的数据归档到K_DELETE_ARCHIVE表（需提前创建），
     * 也可以根据需要自行处理被删数据的归档方式，如果需要自定义，需要实现{@link DeleteArchiveHandler DeleteArchiveHandler}
     * 接口并声明为Spring的Bean，注：不能以@Bean的方式声明，否则无效。
     * @param entityDefName 实体定义编码名
     * @param attrName 作为删除条件的属性名
     * @param attrValues 作为删除条件的数据值
     */
    public void delete(String entityDefName, String attrName, Serializable[] attrValues){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        BeanFieldDef fkFieldDef = getFieldDef(beanEntityDef,attrName);

        delete(attrValues, beanEntityDef, fkFieldDef);
    }

    private void delete(Serializable[] ids, BeanEntityDef beanEntityDef, BeanFieldDef pkFieldDef) {
        Map<String, Object> paramMap = ParamMap.createAndSet("ids", ids).toMap();

        DeleteBuilder deleteBuilder = new DeleteBuilder(beanEntityDef, paramMap);
        deleteBuilder.where(pkFieldDef.getFieldName(), ConditionType.IN, "ids");
//        jdbcEntityDao.delete(deleteBuilder);

        ValueBean valueBean = new ValueBean(beanEntityDef, paramMap, false);
        addOperateObject(OperateType.DELETE,valueBean);

        jdbcEntityDao.execute(deleteBuilder.build());
    }

    /**
     * 根据主键值修改数据的方法。valueBean必须包含主键值。对于修改方法有以下规则：
     * <lu>
     *  <li>指定了name及value，则按照name将值更新为value
     *  <li>指定了name但未指定value（value为null），则将name的字段值更新为null
     *  <li>对于valueBean中未包含的name，不进行修改
     * <lu/>
     * @param valueBean 数据值对象数组
     */
    public void update(ValueBean valueBean){
        BeanEntityDef beanEntityDef = valueBean.getEntityDef();
        update(valueBean,beanEntityDef.getPkFieldDef());
    }

    public void update(ValueBean valueBean,UpdateFragmentTemplate[] fieldTemplate){
        BeanEntityDef beanEntityDef = valueBean.getEntityDef();
        update(valueBean,beanEntityDef.getPkFieldDef(),fieldTemplate);
    }

    private void update(ValueBean valueBean,BeanFieldDef fieldDef){
        update(valueBean,fieldDef,EMPTY_TEMPLATE);
    }

    private void update(ValueBean valueBean,BeanFieldDef fieldDef,UpdateFragmentTemplate[] fieldTemplate){

        fieldFilter(valueBean);
        processFillValue(FillType.UPDATE,valueBean);

        BeanEntityDef beanEntityDef = valueBean.getEntityDef();

        addOperateObject(OperateType.UPDATE,valueBean);

        UpdateBuilder updateBuilder = new UpdateBuilder(beanEntityDef,valueBean.getValueMap(),fieldTemplate);
        updateBuilder.where(fieldDef.getFieldName(),ConditionType.EQUALS,fieldDef.getAttrName());

        jdbcEntityDao.execute(updateBuilder.build());
    }

    /**
     * 根据主键值修改数据的方法。valueBean必须包含主键值。对于修改方法有以下规则：
     * <lu>
     *  <li>指定了name及value，则按照name将值更新为value
     *  <li>指定了name但未指定value（value为null），则将name的字段值更新为null
     *  <li>对于valueBean中未包含的name，不进行修改
     * <lu/>
     * @param entityDefName 实体定义编码名
     * @param valueMap 数据值
     */
    public void update(String entityDefName,Map valueMap){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean valueBean = new ValueBean(beanEntityDef, valueMap,strict);

        update(valueBean,beanEntityDef.getPkFieldDef());
    }

    public void update(String entityDefName,Map valueMap,UpdateFragmentTemplate[] fieldTemplate){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean valueBean = new ValueBean(beanEntityDef, valueMap,strict);

        update(valueBean,fieldTemplate);
    }

    /**
     * 根据指定属性值修改数据的方法。valueBean必须包含attrName的值。对于修改方法有以下规则：
     * <lu>
     *  <li>指定了name及value，则按照name将值更新为value
     *  <li>指定了name但未指定value（value为null），则将name的字段值更新为null
     *  <li>对于valueBean中未包含的name，不进行修改
     * <lu/>
     * @param entityDefName 实体定义编码名
     * @param attrName 作为更新条件的属性值
     * @param valueMap 数据值
     */
    public void update(String entityDefName,String attrName,Map valueMap){
        BeanEntityDef beanEntityDef = getEntityDef(entityDefName);
        ValueBean valueBean = new ValueBean(beanEntityDef, valueMap,strict);

        update(valueBean,attrName);
    }

    /**
     * 根据指定属性值修改数据的方法。valueBean必须包含attrName的值。对于修改方法有以下规则：
     * <lu>
     *  <li>指定了name及value，则按照name将值更新为value
     *  <li>指定了name但未指定value（value为null），则将name的字段值更新为null
     *  <li>对于valueBean中未包含的name，不进行修改
     * <lu/>
     * @param valueBean 实体定义编码名
     * @param attrName 作为更新条件的属性值
     */
    public void update(ValueBean valueBean,String attrName){
        BeanFieldDef fieldDef = valueBean.getEntityDef().getFieldDef(attrName);
        if(fieldDef == null){
            throw new RuntimeException("更新失败，用于条件的属性"+attrName+"不存在");
        }

        update(valueBean,fieldDef);
    }

    /**
     * 根据主键查询唯一的数据信息
     * @param entityDefName 实体定义编码名
     * @param id 主键值
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public ValueMap get(String entityDefName, String id){
        return get(entityDefName,id,null);
    }
    /**
     * 根据主键查询唯一的数据信息
     * @param entityDefName 实体定义编码名
     * @param id 主键值
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public ValueMap get(String entityDefName, String id, FieldFilter filter){
        BeanEntityDef entityDef = getEntityDef(entityDefName);
        String attrName = entityDef.getPkFieldDef().getAttrName();
        return get(entityDefName,attrName,id,filter);
    }

    /**
     * 根据提供的属性名及值查询唯一的数据信息
     * @param entityDefName 实体定义编码名
     * @param attrName 用于条件的属性名
     * @param attrValue 用户条件的属性值
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public ValueMap get(String entityDefName,String attrName, String attrValue, FieldFilter filter){
        Assert.notNull(attrValue,"查询详情必须提供唯一条件值");
        BeanEntityDef entityDef = getEntityDef(entityDefName);
        BeanFieldDef fieldDef = entityDef.getFieldDef(attrName);

        Assert.notNull(fieldDef,entityDefName + "实体中不包含" + attrName + "属性。参数应当提供属性名，是否提供了数据库字段名？");

        Map<String, Object> paramMap = ParamMap.createAndSet(fieldDef.getAttrName(), attrValue).toMap();
        SelectBuilder sqlBuilder = new SelectBuilder(entityDef,paramMap);
        sqlBuilder.where(fieldDef.getFieldName(),ConditionType.EQUALS,fieldDef.getAttrName());

        return get(sqlBuilder.build(),filter);
    }

    /**
     * 根据主键查询唯一的数据信息，根据给定的构造器返回对象
     * @param entityDefName 实体定义编码名
     * @param id 主键值
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(String entityDefName, String id,Function<Map,R> bean){
        BeanEntityDef entityDef = getEntityDef(entityDefName);
        String attrName = entityDef.getPkFieldDef().getAttrName();
        return getForBean(entityDefName,attrName,id,null,bean);
    }

    /**
     * 根据主键查询唯一的数据信息，根据给定的构造器返回对象
     * @param entityDefName 实体定义编码名
     * @param id 主键值
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(String entityDefName, String id, FieldFilter filter,Function<Map,R> bean){
        BeanEntityDef entityDef = getEntityDef(entityDefName);
        String attrName = entityDef.getPkFieldDef().getAttrName();
        return getForBean(entityDefName,attrName,id,filter,bean);
    }

    /**
     * 根据提供的属性名及值查询唯一的数据信息，根据给定的构造器返回对象
     * @param entityDefName 实体定义编码名
     * @param attrName 用于条件的属性名
     * @param attrValue 用于条件的属性值
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(String entityDefName,String attrName, String attrValue, Function<Map,R> bean){
        return getForBean(entityDefName, attrName, attrValue, null,bean);
    }

    /**
     * 根据提供的属性名及值查询唯一的数据信息，根据给定的构造器返回对象
     * @param entityDefName 实体定义编码名
     * @param attrName 用于条件的属性名
     * @param attrValue 用于条件的属性值
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(String entityDefName,String attrName, String attrValue, FieldFilter filter,Function<Map,R> bean){
        ValueMap valueMap = get(entityDefName, attrName, attrValue, filter);
        if(valueMap == null){
            return null;
        }
        return bean.apply(valueMap);
    }

    /**
     * 根据提供的属性名及值查询唯一的数据信息，根据给定的构造器返回对象
     * @param queryBean 查询器对象
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(QuerySupport queryBean, Function<Map,R> bean){
        return getForBean(queryBean,null,bean);
    }

    /**
     * 根据提供的属性名及值查询唯一的数据信息，根据给定的构造器返回对象
     * @param queryBean 查询器对象
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     */
    public <R>  R getForBean(QuerySupport queryBean, FieldFilter filter,Function<Map,R> bean){
        ValueMap valueMap = get(queryBean, filter);
        if(valueMap == null){
            return null;
        }
        return bean.apply(valueMap);
    }


    /**
     * 根据查询器查询唯一的数据信息，如果返回多余1条数据会抛出异常。
     * @param queryBean 查询器对象
     * @param filter 字段过滤器，用于指定查询的字段，可以为null，表示返回所有字段
     * @return 满足条件的数据值，如没有满足条件的数据，则返回null
     * @see SelectBuilder SelectBuilder
     * @see CustomQueryBean CustomQueryBean
     */
    public ValueMap get(QuerySupport queryBean, FieldFilter filter){
        ValueMapList list = list(queryBean, new Page(false),filter);//此处设置new Page()按照分页查询，是避免查询出大批量数据。
        if(list.size() > 1){
            throw new RuntimeException("要求最多返回1条记录，当前返回了多条数据："+list.size());
        }
        if(list.isEmpty()){
            return null;
        }

        return new ValueMap(list.get(0));
    }

    /**
     * 根据查询器查询唯一的数据信息，如果返回多余1条数据会抛出异常。
     * @param queryBean 查询器对象
     * @return 满足条件的数据值（返回所有列值），如没有满足条件的数据，则返回null
     * @see SelectBuilder SelectBuilder
     * @see CustomQueryBean CustomQueryBean
     */
    public ValueMap get(QuerySupport queryBean){
        return get(queryBean, null);
    }

    /**
     * 检查数据是否存在
     * @param queryBean 查询器对象
     * @return true 数据存在，false 数据不存在
     */
    public boolean exist(QuerySupport queryBean){
        long count = jdbcEntityDao.executeCount(queryBean);
        return count > 0;
    }

    /**
     * 得到满足条件的记录数量
     * @param queryBean 查询器对象
     * @return 满足条件的记录数量
     */
    public long count(QuerySupport queryBean){
        return jdbcEntityDao.executeCount(queryBean);
    }

    /**
     * 查询满足条件的所有结果集
     * @param queryBean 查询对象，不能为null
     * @param filter 字段过滤器，为null时表示返回所有可返回的字段
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public ValueMapList list(QuerySupport queryBean, FieldFilter filter){
        return list(queryBean, null, filter);
    }

    /**
     * 查询满足条件的所有结果集
     * @param queryBean 查询对象，不能为null
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public ValueMapList list(QuerySupport queryBean){
        return list(queryBean, null, null);
    }

    /**
     * 分页查询满足条件的结果集
     * @param queryBean 查询对象，不能为null
     * @param page 分页对象，为null时表示返回所有数据，不进行分页逻辑
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public ValueMapList list(QuerySupport queryBean,Page page){
        return list(queryBean, page, null);
    }

    /**
     * 分页查询满足条件的结果集
     * @param queryBean 查询对象，不能为null
     * @param page 分页对象，为null时表示返回所有数据，不进行分页逻辑
     * @param filter 字段过滤器，为null时表示返回所有可返回的字段
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public ValueMapList list(QuerySupport queryBean,Page page, FieldFilter filter){
        List<Map<String, Object>> recordList = executeQuery(queryBean, page, filter);
        return new ValueMapList(recordList);
    }

    public ValueMapList list(QuerySupport queryBean,int maxRow){
        return list(queryBean, maxRow, null);
    }

    public ValueMapList list(QuerySupport queryBean,int maxRow, FieldFilter filter){
        return list(queryBean,0,maxRow,filter);
    }

    public ValueMapList list(QuerySupport queryBean,int firstResult,int maxRow, FieldFilter filter){
        List<Map<String, Object>> recordList = jdbcEntityDao.executeQuery(queryBean, firstResult, maxRow, filter);
        return new ValueMapList(recordList);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean,int maxRow, Function<Map,R> bean){
        return listForBean(queryBean, maxRow, null,bean);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean,int maxRow, FieldFilter filter, Function<Map,R> bean){
        return listForBean(queryBean,0,maxRow,filter,bean);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean,int firstResult,int maxRow, Function<Map,R> bean){
        return listForBean(queryBean,firstResult,maxRow,null,bean);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean,int firstResult,int maxRow, FieldFilter filter, Function<Map,R> bean){
        List<Map<String, Object>> recordList = jdbcEntityDao.executeQuery(queryBean, firstResult, maxRow, filter);
        if(recordList.isEmpty()){
            return Collections.emptyList();
        }

        List<R> resultList = new ArrayList<>(recordList.size());
        for (Map<String, Object> value : recordList) {
            R beanObject = bean.apply(value);
            resultList.add(beanObject);
        }
        return resultList;
    }

    private List<Map<String, Object>> executeQuery(QuerySupport queryBean,Page page, FieldFilter filter){
        Assert.notNull(queryBean,"QueryBean不能为null");
        int firstResult = -1;
        int maxRow = -1;
        if (page != null) {
            if(page.isRecount()){
                int currentPage = page.getCurrentPage();
                long count = jdbcEntityDao.executeCount(queryBean,filter);//这里需要调用带filter的方法

                //如果没有查询出任何数据则直接返回空集合，不需要再执行下面的分页查询
                if(count == 0){
                    return Collections.emptyList();
                }

                page.calculate(count);
                if(page.getMaxPage() < currentPage){
                    return Collections.emptyList();
                }
            }
            firstResult = page.getFirstResult();
            maxRow = page.getPageSize();

        }

        return jdbcEntityDao.executeQuery(queryBean, firstResult, maxRow, filter);
    }


    /**
     * 查询满足条件的所有结果集，根据给定的构造器返回对象
     * @param queryBean 查询对象，不能为null
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean, Function<Map,R> bean){
        return listForBean(queryBean, null,bean);
    }

    /**
     * 分页查询满足条件的结果集，根据给定的构造器返回对象
     * @param queryBean 查询对象，不能为null
     * @param page 分页对象，为null时表示返回所有数据，不进行分页逻辑
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean,Page page, Function<Map,R> bean){
        return listForBean(queryBean, page, null,bean);
    }

    /**
     * 分页查询满足条件的结果集，根据给定的构造器返回对象
     * @param queryBean 查询对象，不能为null
     * @param page 分页对象，为null时表示返回所有数据，不进行分页逻辑
     * @param filter 字段过滤器，为null时表示返回所有可返回的字段
     * @param <R> 返回对象封装类的构造器函数。要求返回类必须包含一个以Map为参数的构造器
     * @return 结果集对象，如果没有满足条件的数据返回空集合，不会返回null
     */
    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean, Page page, FieldFilter filter, Function<Map,R> bean){
        List<Map<String, Object>> list = executeQuery(queryBean, page, filter);
        if(list.isEmpty()){
            return Collections.emptyList();
        }

        List<R> resultList = new ArrayList<>(list.size());
        for (Map<String, Object> value : list) {
            R beanObject = bean.apply(value);
            resultList.add(beanObject);
        }
        return resultList;
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return listForBean(queryBean, bean);
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Page page, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return listForBean(queryBean, page, bean);
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Page page, FieldFilter filter, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return listForBean(queryBean, page, filter, bean);
    }

    /**
     * 根据实体定义编码名获取实体定义对象
     * @param entityDefName 实体定义编码名
     * @return 实体定义对象，如果定义对象不存在则抛出异常
     */
    public final BeanEntityDef getEntityDef(String entityDefName){
        return beanDefDepository.getEntityDef(entityDefName.toUpperCase());
    }

    public final BeanFieldDef getFieldDef(BeanEntityDef beanEntityDef,String attrName){
        BeanFieldDef fieldDef = beanEntityDef.getFieldDef(attrName);
        if(fieldDef == null) {
            throw new RuntimeException("在“" + beanEntityDef.getEntityCode() + "”实体定义对象中不存在属性：" + attrName);
        }
        return fieldDef;
    }
    /**
     * 根据实体定义编码名获取对应实体中的所有字段定义对象。当实体定义不存在时抛出异常。
     * @param entityDefName 实体定义编码名
     * @return 实体定义中的字段定义对象集合
     */
    public final List<BeanFieldDef> getFieldDefList(String entityDefName){
        return beanDefDepository.getFieldDefList(entityDefName);
    }

    /**
     * 实体定义编码名创建一个数据值对象(ValueBean)
     * @param entityName 实体定义编码名
     * @return 数据值对象
     * @deprecated
     */
    protected ValueBean createValueBean(String entityName){
        BeanEntityDef beanEntityDef = getEntityDef(entityName);
        return new ValueBean(beanEntityDef);
    }

    /**
     * 实体定义编码名创建一个数据值对象(ValueBean)，并将valueMap数据赋予到值对象中。
     * 注意：赋值时，仅复制实体定义对象中同名的属性值。
     * @param entityName 实体定义编码名
     * @param valueMap 数据
     * @return 数据值对象
     * @deprecated
     */
    protected ValueBean createValueBean(String entityName,Map<String,Object> valueMap){
        BeanEntityDef beanEntityDef = getEntityDef(entityName);
        return new ValueBean(beanEntityDef,valueMap,false);
    }

    /**
     * 根据查询创建器编码得到查询创建器。
     * @param name 查询创建器编码
     * @param paramMap 查询条件
     * @return 返回指定名城的查询创建器。如果没找到则抛出{@link QueryNotFoundException QueryNotFoundException}异常
     */
    public QuerySupport getQuery(String name,Map<String,Object> paramMap){
        if(paramMap == null){
            paramMap = Collections.emptyMap();
        }
        return queryFactory.getQuery(name,paramMap);
    }

    public QuerySupport getQuery(Class<? extends QueryCreator> queryCreatorClass, Map<String,Object> paramMap){
        if(paramMap == null){
            paramMap = Collections.emptyMap();
        }

        return queryFactory.getQuery(queryCreatorClass,paramMap);
    }

//    /**
//     * 获取在添加和修改方法时，对创造数据值对象(ValueBean)同时赋值时，是否严格检测属性的一一对应关系，即值对象{@link ValueMap ValueMap}中的属性必须是实体定义中的属性。
//     * @return true 严格检测，false 不检测（默认）
//     */
//    public boolean isStrict() {
//        return strict;
//    }
//
//    /**
//     * 设置在添加和修改方法时，对创造数据值对象(ValueBean)同时赋值时，是否严格检测属性的一一对应关系，即值对象{@link ValueMap ValueMap}中的属性必须是实体定义中的属性。
//     * @param strict true 严格检测，false 不检测
//     */
//    public void setStrict(boolean strict) {
//        this.strict = strict;
//    }

    /**
     *
     * @param sqlObject sql执行对象，该方法不会记录审计对象，需要手动处理
     * @return 影响的记录数，返回数组是由于操作可能是批量操作
     */
    public int[] executeUpdate(SqlObject sqlObject){
        return jdbcEntityDao.execute(sqlObject);
    }

    private void processFillValue(FillType type,ValueBean valueBean){
        if(autofillValue == null){
            autofillValue = new StandardFieldAutofill();
        }
        autofillValue.autofill(type,valueBean);
    }

    public void setBeanDefDepository(BeanDefDepository beanDefDepository) {
        this.beanDefDepository = beanDefDepository;
    }

    public void setJdbcEntityDao(JdbcEntityDao jdbcEntityDao) {
        this.jdbcEntityDao = jdbcEntityDao;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setAutofillValue(AutofillValue autofillValue) {
        this.autofillValue = autofillValue;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(beanDefDepository,"beanDefDepository不能为null");
        Assert.notNull(jdbcEntityDao,"jdbcEntityDao不能为null");
        if(idGenerator == null){
            idGenerator = new KduckIdGenerator();
        }
        if(queryFactory == null){
            logger.warn("queryFactory为null，不能执行QueryCreator构造的查询");
        }
    }
}
