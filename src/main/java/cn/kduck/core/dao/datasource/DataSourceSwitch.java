package cn.kduck.core.dao.datasource;

import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DataSourceSwitch {

	private static final ThreadLocal<String> dsKeyThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> cacheKeyThreadLocal = new ThreadLocal<>();

	private static Map<DataSourceMatcher,String> switchMatcherMap = new LinkedHashMap<>();

	private static String[] lookupKeys = null;
	
	private DataSourceSwitch() {}
	
	public static String get(){
        String dataSourceType = dsKeyThreadLocal.get();
        if(dataSourceType == null) {
            dsKeyThreadLocal.set(lookupKeys[0]);
            dataSourceType = lookupKeys[0];
        }
        return dataSourceType;
    }
    
    public static void switchByName(String dsName){
        cacheKeyThreadLocal.set(get());
    	dsKeyThreadLocal.set(dsName);
    }

    /**
     * 根据条件切换数据源，如果未匹配到合适的数据源则抛出异常。
     * @param conditionObj
     */
    public static void switchByCondition(Object conditionObj){
        Assert.notEmpty(switchMatcherMap,"无法根据条件切换数据源，目前没有任何切换条件对象");
        Iterator<DataSourceMatcher> conditionIterator = switchMatcherMap.keySet().iterator();
        while(conditionIterator.hasNext()){
            DataSourceMatcher condition = conditionIterator.next();
            if(condition.supports(conditionObj.getClass()) && condition.match(conditionObj)){
                String dsName = switchMatcherMap.get(condition);
                cacheKeyThreadLocal.set(get());
                dsKeyThreadLocal.set(dsName);
                return;
            }
        }

        throw new RuntimeException("数据源切换失败，没有合适的匹配器："+conditionObj.getClass());
    }

    /**
     * 根据条件切换数据源，如果未匹配到合适的数据源则使用defaultDsName指定的数据源。
     * @param conditionObj
     * @param defaultDsName 当未匹配到合适的数据源则使用该数据源。
     * @return true 匹配到合适的数据源 false 未匹配到合适的数据源
     */
    public static boolean switchByCondition(Object conditionObj,String defaultDsName){
        if(switchMatcherMap.isEmpty()){
            switchByName(defaultDsName);
            return false;
        }
        Iterator<DataSourceMatcher> conditionIterator = switchMatcherMap.keySet().iterator();
        while(conditionIterator.hasNext()){
            DataSourceMatcher condition = conditionIterator.next();
            if(condition.supports(conditionObj.getClass()) && condition.match(conditionObj)){
                String dsName = switchMatcherMap.get(condition);
                cacheKeyThreadLocal.set(get());
                dsKeyThreadLocal.set(dsName);
                return true;
            }
        }
        switchByName(defaultDsName);
        return false;
    }

    /**
     * 切换到上个数据源，该方法仅保留上1个数据源。
     */
    public static void reset(){
	    String key = cacheKeyThreadLocal.get();
	    if(key == null){
            resetDefault();
        }else{
            dsKeyThreadLocal.set(cacheKeyThreadLocal.get());
        }
    }

    /**
     * 将数据源切换到默认数据源，即配置的第一个数据源
     */
    public static void resetDefault(){
        cacheKeyThreadLocal.set(get());
        dsKeyThreadLocal.set(lookupKeys[0]);
    }

    /**
     * 获取默认数据源名，即配置的第一个数据源。
     * @return
     */
    public static String getDefaultDsName(){
        return lookupKeys[0];
    }

    public static void remove(){
        dsKeyThreadLocal.remove();
        cacheKeyThreadLocal.remove();
    }

    public static void addSwitchMatcher(DataSourceMatcher condition, String dsName){
        switchMatcherMap.put(condition,dsName);
    }

    public static boolean isEnabled(){
	    return lookupKeys != null && lookupKeys.length > 0;
    }

    public static boolean hasSwitchMatcher(){
        return !switchMatcherMap.isEmpty();
    }

    public static String[] getLookupKeys() {
        return lookupKeys;
    }

    public static void setLookupKeys(String[] lookupKeys) {
	    if(DataSourceSwitch.lookupKeys != null){
	        throw new RuntimeException("仅允许被设置一次");
        }
        DataSourceSwitch.lookupKeys = lookupKeys;
    }
}
