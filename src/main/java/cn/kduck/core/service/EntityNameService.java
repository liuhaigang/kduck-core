package cn.kduck.core.service;

import cn.kduck.core.dao.FieldFilter;
import cn.kduck.core.dao.SqlObject;
import cn.kduck.core.dao.definition.BeanEntityDef;
import cn.kduck.core.dao.definition.BeanFieldDef;
import cn.kduck.core.dao.query.QueryCreator;
import cn.kduck.core.dao.query.QuerySupport;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * LiuHG
 * 适用于单表增删改查的Service基础类
 */
public abstract class EntityNameService {

    @Autowired
    private DefaultService defaultService;

    protected abstract String entityName();

    public Serializable add(ValueMap valueMap) {
        return defaultService.add(entityName(), valueMap);
    }

    public void delete(String[] ids) {
        defaultService.delete(entityName(),ids);
    }

    public void delete(String attrName, Serializable[] attrValues) {
        defaultService.delete(entityName(), attrName, attrValues);
    }

    public void update(ValueMap valueMap) {
        defaultService.update(entityName(),valueMap);
    }

    public ValueMap get(String id) {
        return defaultService.get(entityName(),id,null);
    }

    public ValueMap get(String id, FieldFilter filter) {
        return defaultService.get(entityName(),id,filter);
    }

    public <R> R getForBean(String id, Function<Map, R> bean) {
        return defaultService.getForBean(entityName(), id, bean);
    }

    public <R> R getForBean(String id, FieldFilter filter, Function<Map, R> bean) {
        return defaultService.getForBean(entityName(), id, filter, bean);
    }

    public <R> R getForBean(String attrName, String id, Function<Map, R> bean) {
        return defaultService.getForBean(entityName(), attrName, id, bean);
    }

    public <R> R getForBean(String attrName, String id, FieldFilter filter, Function<Map, R> bean) {
        return defaultService.getForBean(entityName(), attrName, id, filter, bean);
    }

    public Serializable generateIdValue() {
        return defaultService.generateIdValue();
    }

    public Serializable[] batchAdd(Map[] valueMaps) {
        return defaultService.batchAdd(entityName(), valueMaps);
    }

    public Serializable[] batchAdd(Map[] valueMaps, Map<String, Object> extParams) {
        return defaultService.batchAdd(entityName(), valueMaps, extParams);
    }

    public Serializable[] batchAdd(List<? extends Map> valueMaps) {
        return defaultService.batchAdd(entityName(), valueMaps);
    }

    public Serializable[] batchAdd(List<? extends Map> valueMaps, Map<String, Object> extParams) {
        return defaultService.batchAdd(entityName(), valueMaps, extParams);
    }

    public void update(String attrName, Map valueMap) {
        defaultService.update(entityName(), attrName, valueMap);
    }

    public ValueMapList list(QuerySupport queryBean, FieldFilter filter) {
        return defaultService.list(queryBean, filter);
    }

    public ValueMapList list(QuerySupport queryBean) {
        return defaultService.list(queryBean);
    }

    public ValueMapList list(QuerySupport queryBean, Page page) {
        return defaultService.list(queryBean, page);
    }

    public ValueMapList list(QuerySupport queryBean, Page page, FieldFilter filter) {
        return defaultService.list(queryBean, page, filter);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean, Function<Map, R> bean) {
        return defaultService.listForBean(queryBean, bean);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean, Page page, Function<Map, R> bean) {
        return defaultService.listForBean(queryBean, page, bean);
    }

    public <R extends ValueMap> List<R> listForBean(QuerySupport queryBean, Page page, FieldFilter filter, Function<Map, R> bean) {
        return defaultService.listForBean(queryBean, page, filter, bean);
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return defaultService.listForBean(queryBean, bean);
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Page page, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return defaultService.listForBean(queryBean, page, bean);
    }

    public <R extends ValueMap> List<R> listForBean(Class<? extends QueryCreator> queryClass, Map<String, Object> paramMap, Page page, FieldFilter filter, Function<Map, R> bean) {
        QuerySupport queryBean = getQuery(queryClass, paramMap);
        return defaultService.listForBean(queryBean, page, filter, bean);
    }

    public int[] executeUpdate(SqlObject sqlObject) {
        return defaultService.executeUpdate(sqlObject);
    }

    protected final BeanEntityDef getEntityDef(String entityDefName) {
        return defaultService.getEntityDef(entityDefName);
    }

    protected final List<BeanFieldDef> getFieldDefList(String entityDefName) {
        return defaultService.getFieldDefList(entityDefName);
    }

    protected QuerySupport getQuery(Class<? extends QueryCreator> queryCreatorClass, Map<String, Object> paramMap) {
        return defaultService.getQuery(queryCreatorClass, paramMap);
    }
}
