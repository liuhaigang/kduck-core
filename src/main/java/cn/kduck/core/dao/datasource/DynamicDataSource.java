package cn.kduck.core.dao.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
//        String dataSourceType = DataSourceSwitch.get();
//        if(dataSourceType == null) {
//            DataSourceSwitch.reset();
//            dataSourceType = DataSourceSwitch.get();
//        }
        return DataSourceSwitch.get();
    }
}
