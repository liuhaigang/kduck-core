package cn.kduck.core.dao.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 框架数据访问层JDBC方面的工具类
 * @author LiuHG
 */
public abstract class JdbcUtils {

    public static final String PLACEHOLDER_PATTERN = "#\\{([\\w]*)\\}";

    private static Pattern pattern = Pattern.compile(PLACEHOLDER_PATTERN);

    /**
     * 根据SQL中的占位符从左到右的顺序，以此返回对应的参数值集合。
     * @param sql 包含占位符的SQL语句
     * @param paramMap 参数值Map对象
     * @return 与SQL中占位符出现顺序一致的参数值集合。
     */
    public static List<Object> getValueList(String sql, Map<String, Object> paramMap){
        return getValueList(getNameList(sql),paramMap);
    }

    /**
     * 根据SQL中的占位符从左到右的顺序，以此返回对应的参数值集合。
     * @param paramNameList 按照从左到右的参数占位符属性名列表
     * @param paramMap 参数值Map对象
     * @return 与SQL中占位符出现顺序一致的参数值集合。
     */
    public static List<Object> getValueList(List<String> paramNameList, Map<String, Object> paramMap){
        List<Object> valueList = new ArrayList<>();

        for (String paramName : paramNameList) {
            if(!paramMap.containsKey(paramName)){
                throw new IllegalArgumentException("未给参数"+paramName+"提供值");
            }
            Object paramValue = paramMap.get(paramName);
            if(paramValue != null) {
                Class<?> paramClass = paramValue.getClass();
                if(paramValue instanceof Collection){
                    valueList.addAll((Collection)paramValue);
                }else if(paramClass.isArray()){
                    valueList.addAll(Arrays.asList((Object[])paramValue));
                }else{
                    valueList.add(paramValue);
                }
            }

        }

        return valueList;
//        List<Object> valueList = new ArrayList<>();
//
//        Matcher matcher = pattern.matcher(sql);
//
//        List<Object> conditionList = new ArrayList<>();
//
//        while(matcher.find()) {
//            String placeholder = matcher.group(1);
//
//            if(!paramMap.containsKey(placeholder)){
//                throw new IllegalArgumentException("未给参数"+placeholder+"提供值，sql：" + sql);
//            }
//
//            Object value = paramMap.get(placeholder);
//            boolean isCondition = false;
//            for (int i = 0; i < conditionNameList.size(); i++) {
//                String name = conditionNameList.get(i);
//                if(name.equals(placeholder) || placeholder.startsWith(name+'_')){
//                    conditionList.add(value);
//                    isCondition = true;
//                    break;
//                }
//            }
//            if(!isCondition){
//                valueList.add(value);
//            }
//        }
//
//        valueList.addAll(conditionList);
//
//        return valueList;
    }

    public static List<String> getNameList(String sql){
        List<String> nameList = new ArrayList<>();

        Matcher matcher = pattern.matcher(sql);

//        List<Object> conditionList = new ArrayList<>();

        while(matcher.find()) {
            String placeholder = matcher.group(1);
            nameList.add(placeholder);
        }
        return nameList;
    }
}
