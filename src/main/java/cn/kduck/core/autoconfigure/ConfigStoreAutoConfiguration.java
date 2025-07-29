package cn.kduck.core.autoconfigure;

import cn.kduck.core.configstore.ConfigStoreReloader;
import cn.kduck.core.configstore.scan.ConfigStoreScannerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
@ConditionalOnBean(ConfigStoreReloader.class)
public class ConfigStoreAutoConfiguration {

    @Bean
    public static ConfigStoreScannerConfigurer configStoreScannerConfigurer(){
        return new ConfigStoreScannerConfigurer();
    }
}
