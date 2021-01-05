package cn.kduck.core.dao.datasource;

public interface DataSourceMatcher<T> {

    boolean supports(Class cls);

    boolean match(T conditionObj);
}
