package cn.kduck.core.dao.query;

import cn.kduck.core.dao.definition.BeanDefDepository;

import java.util.Map;

/**
 * 查询对象Query构造器
 * @author LiuHG
 */
public interface QueryCreator {

    default String queryCode(){
        return getClass().getSimpleName();
    }

    /**
     * 创建构造Query对象
     * @param paramMap 查询参数对象，该对象不会为null，不用做非null判断。
     * @param depository
     * @return QuerySupport
     */
    QuerySupport createQuery(Map<String, Object> paramMap, BeanDefDepository depository);

}
