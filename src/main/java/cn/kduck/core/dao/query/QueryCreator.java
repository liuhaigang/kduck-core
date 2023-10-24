package cn.kduck.core.dao.query;

import cn.kduck.core.dao.definition.BeanDefDepository;

import java.util.Map;

/**
 * 查询对象Query构造器，该接口不能用于增删改，仅用于拼装查询sql的QuerySupport对象。通常做法是在该方法中构造Selectbuilder
 * d
 * @author LiuHG
 */
public interface QueryCreator {

    default String queryCode(){
        return getClass().getName();
    }

    /**
     * 创建构造Query对象
     * @param paramMap 查询参数对象，该对象不会为null，不用做非null判断。
     * @param depository
     * @return QuerySupport
     */
    QuerySupport createQuery(Map<String, Object> paramMap, BeanDefDepository depository);

}
