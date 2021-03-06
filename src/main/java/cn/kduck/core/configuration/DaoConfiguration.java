package cn.kduck.core.configuration;

import cn.kduck.core.dao.DefaultDeleteArchiveHandler;
import cn.kduck.core.dao.DeleteArchiveHandler;
import cn.kduck.core.dao.JdbcEntityDao;
import cn.kduck.core.dao.datasource.DataSourceMatcher;
import cn.kduck.core.dao.datasource.DataSourceSwitch;
import cn.kduck.core.dao.datasource.DynamicDataSource;
import cn.kduck.core.dao.datasource.DynamicDataSourceProperties;
import cn.kduck.core.dao.datasource.condition.RequestMethodMatcher;
import cn.kduck.core.dao.definition.BeanDefDepository;
import cn.kduck.core.dao.definition.BeanDefSource;
import cn.kduck.core.dao.definition.DefaultFieldAliasGenerator;
import cn.kduck.core.dao.definition.DefaultTableAliasGenerator;
import cn.kduck.core.dao.definition.FieldAliasGenerator;
import cn.kduck.core.dao.definition.MemoryBeanDefDepository;
import cn.kduck.core.dao.definition.TableAliasGenerator;
import cn.kduck.core.dao.definition.impl.JdbcBeanDefSource;
import cn.kduck.core.dao.id.IdGenerator;
import cn.kduck.core.dao.id.impl.SnowFlakeGenerator;
import cn.kduck.core.dao.id.impl.SnowFlakeGenerator.SnowFlakeProperties;
import cn.kduck.core.utils.StringUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.sql.DataSource;
import java.util.*;
import java.util.Map.Entry;

/**
 * LiuHG
 */
@Configuration
public class DaoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator(SnowFlakeProperties snowFlakeProperties){
       return new SnowFlakeGenerator(snowFlakeProperties.getWorkerId(),snowFlakeProperties.getDataCenterId(),snowFlakeProperties.getSequence());
    }

    @Bean
    @ConditionalOnMissingBean(FieldAliasGenerator.class)
    public FieldAliasGenerator attrNameGenerator(){
        return new DefaultFieldAliasGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(TableAliasGenerator.class)
    public TableAliasGenerator tableAliasGenerator(){
        return new DefaultTableAliasGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(BeanDefDepository.class)
    public BeanDefDepository beanDefDepository(@Lazy List<BeanDefSource> beanDefSourceList){
        return new MemoryBeanDefDepository(beanDefSourceList);
    }

    @Bean
    @ConditionalOnMissingBean(BeanDefSource.class)
    public BeanDefSource beanDefSource(){
        return new JdbcBeanDefSource();
    }

    @Bean
    @ConditionalOnMissingBean(DeleteArchiveHandler.class)
    public DeleteArchiveHandler deleteArchiveHandler(){
        return new DefaultDeleteArchiveHandler();
    }

    @Bean
    @ConditionalOnMissingBean(JdbcEntityDao.class)
    public JdbcEntityDao jdbcEntityDao(){
        return new JdbcEntityDao();
    }

//    @Bean
////    @Primary
//    @ConfigurationProperties("spring.datasource")
//    public DataSourceProperties dataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Bean
//    @ConfigurationProperties("spring.datasource.hikari")
//    public DataSource mainDataSource(DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//    }

    @Bean
    @Conditional(SpringDataSourceUnavailableCondition.class)
    @Primary
    public DataSource dataSource(DynamicDataSourceProperties properties) {

        DynamicDataSource dataSource = new DynamicDataSource();
        Map<Object,Object> dataSourceMap = new HashMap<>();

        DataSource defaultDataSource = null;

        Map dsPropMap = properties.getDataSource();

        if(dsPropMap.isEmpty()) {
            throw new RuntimeException("???????????????????????????????????????1??????????????????");
        }

        String[] lookupKeys = new String[dsPropMap.size()];
        Iterator keyNames = dsPropMap.keySet().iterator();

        int index = 0 ;
        while(keyNames.hasNext()){
            Object dsKey = keyNames.next();
            Map propMap = (Map) dsPropMap.get(dsKey);

            lookupKeys[index] = dsKey.toString();
            index++;

            Object url = propMap.remove("url");
            if(url != null) {
                propMap.put("jdbcUrl",url);
            }

            Properties dsProperties = convertProperties(propMap);

            Map matchMap = (Map) dsProperties.remove("match");
            if(matchMap != null){
                String matchMethod = (String)matchMap.get("requestMethod");
                String matcherClass = (String)matchMap.get("matcherClass");
                if(matchMethod != null){
                    DataSourceSwitch.addSwitchMatcher(new RequestMethodMatcher(matchMethod.split("[,;]")),dsKey.toString());
                }
                if(matcherClass != null){
                    String[] matcherClasses = matcherClass.split("[,;]");
                    for (String mclsName : matcherClasses) {
                        Class<? extends DataSourceMatcher> mcls = null;
                        try {
                            mcls = (Class<? extends DataSourceMatcher>) Class.forName(mclsName);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("???????????????????????????????????????" + mclsName +
                                    "??????????????????????????????matcher-class????????????????????????",e);
                        }
                        if(!DataSourceMatcher.class.isAssignableFrom(mcls)){
                            throw new RuntimeException("???????????????????????????????????????" + mclsName +
                                    "???????????????DataSourceMatcher??????????????????");
                        }
                        DataSourceSwitch.addSwitchMatcher(BeanUtils.instantiateClass(mcls),dsKey.toString());
                    }

                }
            }

            HikariDataSource hikariDataSource = new HikariDataSource(new HikariConfig(dsProperties));

            if(defaultDataSource == null){
                defaultDataSource = hikariDataSource;
                dataSourceMap.put(lookupKeys[0],hikariDataSource);
            }

            dataSourceMap.put(dsKey,hikariDataSource);
        }

        DataSourceSwitch.setLookupKeys(lookupKeys);

        dataSource.setTargetDataSources(dataSourceMap);
        dataSource.setDefaultTargetDataSource(defaultDataSource);
        return dataSource;
    }

    private Properties convertProperties(Map propMap) {
        Properties dsProperties = new Properties();
        Set<Entry> entrySet = propMap.entrySet();
        for (Entry entry : entrySet) {
            String name = entry.getKey().toString();
            Object value = entry.getValue();
            if(value instanceof Map){
                value = new LinkedHashMap(convertProperties((Map) value));
            }
            String[] keySplit = name.split("-");
            if(keySplit.length > 1){
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 0; i < keySplit.length; i++) {
                    if(i == 0){
                        nameBuilder.append(keySplit[i]);
                    }else{
                        nameBuilder.append(StringUtils.upperFirstChar(keySplit[i]));
                    }

                }
                dsProperties.put(nameBuilder.toString(), value);
            }else{
                dsProperties.put(name, value);
            }
        }
        return dsProperties;
    }

    public static class SpringDataSourceUnavailableCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            String url = environment.getProperty("spring.datasource.url");
            String jndi = environment.getProperty("spring.datasource.jndi-name");

            boolean match = org.springframework.util.StringUtils.hasText(url) || org.springframework.util.StringUtils.hasText(jndi);

            if(match){
                return new ConditionOutcome(false,"???????????????SpringDataSource");
            }

            return new ConditionOutcome(true,"???????????????K-Duck DataSource????????????????????????");
        }
    }


}
