package cn.kduck.core.dao.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("kduck")
public class DynamicDataSourceProperties {

    private Map dataSource;

    public Map getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map dataSource) {
        this.dataSource = dataSource;
    }
}
