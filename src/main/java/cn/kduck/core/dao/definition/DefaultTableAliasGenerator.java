package cn.kduck.core.dao.definition;

import cn.kduck.core.dao.datasource.DataSourceSwitch;

/**
 * LiuHG
 */
public class DefaultTableAliasGenerator implements TableAliasGenerator {

    @Override
    public String genAlias(String tableName) {
//        String[] nameSplit = tableName.toLowerCase().split("_");
//        StringBuilder nameBuilder = new StringBuilder(nameSplit[0]);
//        for (int i = 1; i < nameSplit.length; i++) {
//            nameBuilder.append(StringUtils.upperFirstChar(nameSplit[i]));
//        }
//        return nameBuilder.toString();

        String lookupKey = null;
        if(DataSourceSwitch.isEnabled()){
            lookupKey = DataSourceSwitch.get();
        }

        lookupKey = lookupKey == null ? "" : lookupKey + ".";
        return lookupKey + tableName.toUpperCase();

    }
}
