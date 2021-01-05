package cn.kduck.core.dao.definition;

import cn.kduck.core.utils.StringUtils;

/**
 * 默认的实体字段名称别名生成器，根据字段以驼峰格式返回字段属性名
 * @author LiuHG
 */
public class DefaultFieldAliasGenerator implements FieldAliasGenerator {

    @Override
    public String genAlias(String dataSourceKey,String table,String fieldName) {
        String[] nameSplit = fieldName.toLowerCase().split("_");
        StringBuilder nameBuilder = new StringBuilder(nameSplit[0]);
        for (int i = 1; i < nameSplit.length; i++) {
            nameBuilder.append(StringUtils.upperFirstChar(nameSplit[i]));
        }
        return nameBuilder.toString();
    }
}
