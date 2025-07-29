package cn.kduck.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("kduck")
public class KduckProperties {

    private ShowSqlProperties showSql;

    private ResourceProperties resource;

    private WebsocketProperties websocket;

    private EntityDefinitionProperties definition;

    private ExceptionProperties exception;

    //######### 多数据源配置 #############
    private Map dataSource;

    public ExceptionProperties getException() {
        return exception;
    }

    public void setException(ExceptionProperties exception) {
        this.exception = exception;
    }

    public Map getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map dataSource) {
        this.dataSource = dataSource;
    }

    public ResourceProperties getResource() {
        return resource;
    }

    public void setResource(ResourceProperties resource) {
        this.resource = resource;
    }

    public ShowSqlProperties getShowSql() {
        return showSql;
    }

    public void setShowSql(ShowSqlProperties showSql) {
        this.showSql = showSql;
    }

    public WebsocketProperties getWebsocket() {
        return websocket;
    }

    public void setWebsocket(WebsocketProperties websocket) {
        this.websocket = websocket;
    }

    public EntityDefinitionProperties getDefinition() {
        if(definition == null){
            return new EntityDefinitionProperties();
        }
        return definition;
    }

    public void setDefinition(EntityDefinitionProperties definition) {
        this.definition = definition;
    }
    //子配置对象

    public static class WebsocketProperties{
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class EntityDefinitionProperties{
        private ScanTablesProperties tables;

        public ScanTablesProperties getTables() {
            if(tables == null){
                return new ScanTablesProperties();
            }
            return tables;
        }

        public void setTables(ScanTablesProperties tables) {
            this.tables = tables;
        }
    }

    public static class ScanTablesProperties{
        private String[] exclude;
        private String[] include;

        public String[] getExclude() {
            return exclude == null ? new String[0] : exclude;
        }

        public void setExclude(String[] exclude) {
            this.exclude = exclude;
        }

        public String[] getInclude() {
            return include == null ? new String[0] : include;
        }

        public void setInclude(String[] include) {
            this.include = include;
        }
    }

    public static class ResourceProperties{
        private String resourceGroup;
        private boolean enabled;
        private boolean skipInJar = true;
        private String[] basePackage;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String[] getBasePackage() {
            return basePackage;
        }

        public void setBasePackage(String[] basePackage) {
            this.basePackage = basePackage;
        }

        public boolean isSkipInJar() {
            return skipInJar;
        }

        public void setSkipInJar(boolean skipInJar) {
            this.skipInJar = skipInJar;
        }

        public String getResourceGroup() {
            return resourceGroup;
        }

        public void setResourceGroup(String resourceGroup) {
            this.resourceGroup = resourceGroup;
        }
    }

    public static class ShowSqlProperties{

        private boolean enabled;
        private ShowSqlMode mode = ShowSqlMode.SQL;
        private long slowThreshold = 500;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ShowSqlMode getMode() {
            return mode;
        }

        public void setMode(ShowSqlMode mode) {
            this.mode = mode;
        }

        public long getSlowThreshold() {
            return slowThreshold;
        }

        public void setSlowThreshold(long slowThreshold) {
            this.slowThreshold = slowThreshold;
        }
    }

    public enum ShowSqlMode {
        SQL,
        TIME_SQL,
        JUST_SLOW_SQL,
        SQL_ON_ERROR;
    }

    public static class ExceptionProperties{
        private boolean includeMessage = false;
        private ExceptionNoticeType noticeType;

        public boolean isIncludeMessage() {
            return includeMessage;
        }

        public void setIncludeMessage(boolean includeMessage) {
            this.includeMessage = includeMessage;
        }

        public ExceptionNoticeType getNoticeType() {
            return noticeType;
        }

        public void setNoticeType(ExceptionNoticeType noticeType) {
            this.noticeType = noticeType;
        }
    }

    public enum ExceptionNoticeType {
        NONE,
        EMAIL,
        SMS,
        API;
    }
}
